package com.zfy.service;

import org.springframework.stereotype.Service;
import java.util.*;
import static com.zfy.service.JsonSupport.*;
import static com.zfy.service.BusinessStore.*;
import static com.zfy.service.PlanningService.checkUnchanged;

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
 */

/*
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * @Service把本类交给Spring管理；本类只有一个构造器时，Spring自动用它注入依赖，无需再写@Autowired。
 */

/**
 * 管理任务状态、仿真、完成和遥测；关联更新与事件记录在同一短事务中完成。
 */
@Service
public class MissionService {
    // store：业务存储组件，统一处理MySQL读写、事务和计算上下文。
    private final BusinessStore store;
    private final FrameCache frameCache;

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param store 业务存储组件，统一处理MySQL读写、事务和计算上下文
     */
    public MissionService(BusinessStore store,FrameCache frameCache) {
        this.frameCache=frameCache;
        this.store=store;
    }


    /*
     * 事务Lambda：() -> {...}是无参回调，交给store.tx执行；大括号内的return是回调的返回值。
     * 先完成网络计算，再把关联写库放入短事务，减少长期持有数据库锁的时间。
     */

    /**
     * 校验关联规划、航线及算法版本，创建planned状态的仿真任务。
     *
     * @param input 创建任务请求，包含规划ID、航线标识和任务描述
     */
    public Map<String,Object> create(Object input) {
        // p：任务创建请求的Map形式。
        var p=map(input);
        length(p.get("name"),180,"name");
        length(p.get("task_type"),60,"task_type");
        length(p.get("route_key"),50,"route_key");
        // run：任务所关联的规划数据库记录。
        var run=store.get("planning_runs",number(p.get("plan_id")));
        // result：规划result_json解析出的算法结果。
        var result=objectJson(run.get("result_json"));
        // route：与请求route_key匹配的航线；找不到时为null。
        var route=rows(result.get("routes")).stream().filter(/* r：当前检查的候选航线Map。 */ r->Objects.equals(r.get("key"),p.get("route_key"))).findFirst().orElse(null);
        require(route!=null,422,"指定航线不存在");
        // version：计算服务当前使用的算法版本号。
        var version=store.calculate("rules",obj()).get("algorithm");
        require(Objects.equals(version,run.get("algorithm_version")) && yes(route.get("feasible")) && yes(route.get("reserve_satisfied")),
            409,"航线未通过当前版本约束校验，请重新规划");
        return store.tx(()-> {
            // row：新插入并回查的任务记录。
            var row=store.insert("missions",obj("name",p.get("name"),"task_type",p.get("task_type"),"plan_id",p.get("plan_id"),
                "route_key",p.get("route_key"),
                "status","planned","execution_mode","SIMULATION","drone_json",objectJson(run.get("request_json")).get("drone")));
            return createdView(row);
        });
    }

    /**
     * 将任务数据库行转换为创建接口需要的摘要字段。
     *
     * @param r 需要转换为摘要的任务数据库记录
     */
    private Map<String,Object> createdView(Map<String,Object> r) {
        return obj("id",r.get("id"),"name",r.get("name"),"status",r.get("status"),"execution_mode",r.get("execution_mode"),
            "created_at",iso(r.get("created_at")));
    }

    /**
     * 读取任务列表并补充任务类型、规划ID和航线标识。
     */
    public List<Map<String,Object>> list() {
        return store.list("missions").stream().map(/* r：当前遍历的任务数据库记录。 */ r->{
            // v：当前任务的响应摘要Map，随后加入关联规划和航线信息。
            var v=createdView(r);
            v.putAll(obj("task_type",r.get("task_type"),"plan_id",r.get("plan_id"),"route_key",r.get("route_key")));
            return v;
        }).toList();
    }

    /**
     * 把任务、规划和限制区快照交给计算服务生成仿真结果。
     *
     * @param id 要生成仿真结果的任务主键
     */

    private Map<String,Object> simulateCalculation(long id) {
        return store.calculate("simulate",obj(),store.contextForMission(id),id);
    }
    /**
     * 先计算仿真结果，再锁定并检查任务未改变，最后更新为running。
     *
     * @param id 要启动/恢复仿真的任务主键
     */
    public Object simulate(long id) {
        // before：计算开始前的任务快照，用于检查状态并防止并发覆盖。
        var before=store.get("missions",id);
        require(Set.of("planned","running","paused").contains(before.get("status")),409,"该任务已结束，请创建新任务");
        // response：本次仿真计算响应，含data与本次使用的算法版本。
        var response=simulateCalculation(id);
        // data：仿真结果，含route和frames。
        var data=map(response.get("data"));
        // frames：轨迹帧列表，稍后缓存起来给完成流程复用。
        var frames=rows(data.get("frames"));
        require(frames!=null&&!frames.isEmpty(),502,"计算服务未返回仿真轨迹帧");
        // 缓存在事务外写入：Redis是网络调用，不应该占着数据库事务不提交。
        frameCache.put(before,response.get("algorithm"),frames);
        return store.tx(()->{
            checkUnchanged(before,store.lock("missions",id));
            store.update("missions",id,obj("status","running"));
            return data;
        });
    }

    /*
     * Map.of和Set.of创建不可修改的映射与集合，此处用来表达状态机，不是数据库表。
     * getOrDefault在当前状态不存在时返回空集合，contains(target)自然为false，从而拒绝非法转换。
     */

    /**
     * 在任务行锁内校验状态转换，更新任务并保存状态事件。
     *
     * @param id 要变更状态的任务主键
     * @param target 希望转换到的任务状态，如paused、running或cancelled
     */
    public Object transition(long id,String target) {
        return store.tx(()-> {
            // row：FOR UPDATE锁定的任务当前记录。
            var row=store.lock("missions",id);
            // allowed：状态转换表，键为当前状态，值为允许到达的目标状态集合。
            var allowed=Map.of("planned",Set.of("cancelled"),"running",Set.of("paused","cancelled"),"paused",Set.of("running",
                "cancelled"));
            require(allowed.getOrDefault(row.get("status"),Set.of()).contains(target),409,"不允许此任务状态转换");
            store.update("missions",id,obj("status",target));
            store.event("mission_state",id,obj("before",row.get("status"),"after",target));
            return obj("id",id,"status",target);
        });
    }

    /*
     * 重复请求处理：prior是在耗时计算前查到的完成事件；done是在拿到任务锁后重新查到的事件。
     * 后一次检查用于处理两个请求同时通过前一次检查的情况，确保不会各自插入一份完成日志。
     * store.tx中的return返回给Lambda调用方，不是从外层方法的任意位置跳出。
     */

    /**
     * 完成仿真、归档日志并保存事件；锁内再次检查完成事件以防重复入库。
     *
     * @param id 要完成的任务主键
     */
    public Object complete(long id) {
        // before：生成完成日志前的任务快照。
        var before=store.get("missions",id);
        // prior：计算前查到的已有完成事件，存在时直接复用其结果。
        var prior=store.lastEvent("mission_completed",id);
        if(prior!=null) return parse(prior.get("detail_json"));
        require("running".equals(before.get("status")),409,"仅运行中的仿真任务可以完成");

        // version：任务当前航线所属的算法版本，用来判断缓存是否仍然适用。本地SQL一次查询，无额外网络。
        var version=store.get("planning_runs",number(before.get("plan_id"))).get("algorithm_version");
// frames：优先复用Redis里的仿真帧；未命中或已失效时回退到重新计算。
        var frames=frameCache.get(before,version);
        if(frames==null) frames=rows(map(simulateCalculation(id).get("data")).get("frames"));
// logRequest：使用仿真轨迹组装的日志计算请求。
        var logRequest=obj("name",before.get("name")+" · 仿真执行日志","source_name","轨迹插值仿真（非飞控仿真）","is_simulation",true,"points",frames);

        length(logRequest.get("name"),180,"日志名称");
        // computed：日志指标计算响应，含待保存的log产物。
        var computed=store.calculate("replay",logRequest);
        return store.tx(()-> {
            // locked：加锁后的最新任务记录，用于检查计算期间是否被修改。
            var locked=store.lock("missions",id);
            // done：取得任务行锁后再次查询的完成事件，用于防止并发重复完成。
            var done=store.lastEvent("mission_completed",id);
            if(done!=null) return parse(done.get("detail_json"));
            checkUnchanged(before,locked);
            // log：正式保存的飞行日志记录，含MySQL生成的id。
            var log=store.insert("flight_logs",store.artifact(computed,"log"));
            store.update("missions",id,obj("status","completed"));
            // result：完成任务的响应，包含mission_id、log_id、状态和指标。
            var result=obj("mission_id",id,"status","completed","log_id",log.get("id"),"metrics",parse(log.get("metrics_json")));
            store.event("mission_completed",id,result);
            return result;
        });
    }

    /**
     * 计算遥测指标，加锁确认任务未改变，再保存日志和导入事件。
     *
     * @param id 遥测数据关联的任务主键
     * @param payload 遥测批次请求，包含需要归档的观测点
     */
    public Object telemetry(long id,Object payload) {
        // before：遥测计算前的任务快照。
        var before=store.get("missions",id);
        // response：遥测分析完整响应，含data和log产物。
        var response=store.calculate("telemetry",payload,store.context("missions","planning_runs"),id);
        // logData：计算生成的待入库日志字段。
        var logData=store.artifact(response,"log");
        length(logData.get("name"),180,"日志名称");
        return store.tx(()-> {
            checkUnchanged(before,store.lock("missions",id));
            // log：保存后带正式主键的日志记录。
            var log=store.insert("flight_logs",logData);
            // result：遥测响应Map，替换临时log_id后返回。
            var result=map(response.get("data"));
            result.put("log_id",log.get("id"));
            store.event("telemetry_imported",id,result);
            return result;
        });
    }
}
