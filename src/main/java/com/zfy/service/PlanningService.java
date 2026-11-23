package com.zfy.service;

import com.zfy.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.util.*;
import static com.zfy.service.JsonSupport.*;
import static com.zfy.service.BusinessStore.*;


/*
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
 */

/**
 * 管理环境查询、限制区、规划快照和执行中重规划。
 */
@Service
public class PlanningService {
    // store：业务存储组件，统一处理MySQL读写、事务和计算上下文。
    private final BusinessStore store;

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param store 业务存储组件，统一处理MySQL读写、事务和计算上下文
     */
    public PlanningService(BusinessStore store) {
        this.store=store;
    }

    /**
     * 将环境查询字段适配为计算服务参数并查询环境。
     *
     * @param query 环境查询DTO或对象，包含边界与数据模式
     */
    public Object environment(Object query) {
        // p：环境查询的Map形式，在这里把dataMode改为data_mode。
        var p=map(query);
        // EnvironmentQueryDTO不是JSON请求，dataMode在这里显式映射。
        if(p.containsKey("dataMode")) p.put("data_mode",p.remove("dataMode"));
        return store.calculate("environment",p,"data_source_snapshots").get("data");
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 筛选当前生效的限制区并转换其展示字段。
     */
    public List<Map<String,Object>> restrictions() {
        return store.list("restriction_zones").stream()
        .filter(/* r：当前筛选或转换的限制区数据库记录。 */ r->(r.get("effective_from")==null || !time(r.get("effective_from")).isAfter(now()))
            && (r.get("effective_to")==null || !time(r.get("effective_to")).isBefore(now())))
        .map(/* r：当前筛选或转换的限制区数据库记录。 */ r->restrictionView(r)).toList();
    }

    /**
     * 复制限制区数据，解析GeoJSON并格式化创建时间，去掉有效期字段。
     *
     * @param r 原始限制区数据库行
     */
    private Map<String,Object> restrictionView(Map<String,Object> r) {
        // v：用于展示的限制区副本，避免改变原始行。
        var v=new LinkedHashMap<>(r);
        v.remove("effective_from");
        v.remove("effective_to");
        v.put("geojson",parse(r.get("geojson")));
        v.put("created_at",iso(r.get("created_at")));
        return v;
    }

    /*
     * 事务Lambda：() -> {...}是无参回调，交给store.tx执行；大括号内的return是回调的返回值。
     * 先完成网络计算，再把关联写库放入短事务，减少长期持有数据库锁的时间。
     */

    /**
     * 校验长度、高度与有效期，调用几何校验后保存限制区。
     *
     * @param input 用户提交的限制区导入请求
     */
    public Map<String,Object> importRestriction(Object input) {
        // p：导入请求转换成的Map，便于校验字段。
        var p=map(input);
        length(p.get("name"),180,"name");
        length(p.get("source_name"),180,"source_name");
        length(p.get("source_url"),500,"source_url");
        require(((Number)p.get("max_altitude_m")).doubleValue()>=((Number)p.get("min_altitude_m")).doubleValue(),422,"最高高度不能低于最低高度");
        try {
            if(p.get("effective_from")!=null && p.get("effective_to")!=null)
            require(!time(p.get("effective_to")).isBefore(time(p.get("effective_from"))),422,"有效期结束不能早于开始");
        } catch(/* e：限制区有效期文本无法转换为日期时间的异常。 */ java.time.DateTimeException e) {
            throw new BusinessException(422,"有效期必须为ISO日期时间");
        }
        // response：几何校验后的完整计算响应，包含restriction产物。
        var response=store.calculate("restriction",p);
        return store.tx(()-> {
            // row：保存到MySQL后回查的限制区记录。
            var row=store.insert("restriction_zones",store.artifact(response,"restriction"));
            return obj("id",row.get("id"),"name",row.get("name"),"category",row.get("category"),"source_name",row.get("source_name"),
                "created_at",iso(row.get("created_at")));
        });
    }

    /**
     * 计算新规划，并在短事务内保存规划快照。
     *
     * @param input 新规划请求，包含起终点、航点、无人机和约束
     */
    public Map<String,Object> create(Object input) {
        // response：航线规划的完整计算响应，包含待保存的plan产物。
        var response=store.calculate("plan",input,"restriction_zones","data_source_snapshots");
        return store.tx(()->save(response));
    }

    /**
     * 保存计算响应中的规划产物，使用正式数据库ID构造结果；事务由调用方控制。
     *
     * @param envelope 计算服务完整响应，包含data、artifacts、sources等部分
     */
    public Map<String,Object> save(Map<String,Object> envelope) {
        // row：插入planning_runs后回查的正式规划记录。
        var row=store.insert("planning_runs",store.artifact(envelope,"plan"));
        return view(row,false);
    }

    /**
     * 读取指定规划及原始请求快照。
     *
     * @param id 要查询的规划主键
     */
    public Map<String,Object> get(long id) {
        return view(store.get("planning_runs",id),true);
    }

    /**
     * 合并规划结果与环境快照，按需加入原始请求。
     *
     * @param row 用于组装响应的规划数据库行
     * @param includeRequest 是否在规划详情中附带原始规划请求
     */
    public Map<String,Object> view(Map<String,Object> row,boolean includeRequest) {
        // env：解析后的环境快照，包含天气、建筑、来源和实际模式。
        var env=objectJson(row.get("environment_json"));
        // result：解析后的规划结果Map，将补充正式ID、时间及环境信息。
        var result=objectJson(row.get("result_json"));
        result.putAll(obj("plan_id",row.get("id"),"created_at",iso(row.get("created_at")),"sources",env.getOrDefault("sources",
            List.of()),
            "weather",env.get("weather"),"buildings",env.getOrDefault("buildings",List.of()),"data_mode_used",env.getOrDefault("data_mode_used",
            "real"),"simulation_notice",env.get("simulation_notice")));
        if(includeRequest) result.put("request",parse(row.get("request_json")));
        return result;
    }

    /*
     * Lambda捕获：store.tx(() -> {...})把一段业务作为回调传入，回调执行时才做里面的数据库操作。
     * 它使用外部的missionId、before等变量；被捕获的局部变量必须是final或事实上只赋值一次。
     * Map引用不重新赋值仍可调用put/remove修改内容，这与“变量引用不能重新赋值”不是一回事。
     * 失败分支先让暂停任务和保存风险的事务正常返回，再在事务外throw e，避免这些保护性操作一起回滚。
     */

    /*
     * 三元表达式：条件 ? 值A : 值B表示条件成立取A，否则取B。
     * 这里只计算被选中的分支，常用于处理可空字段或选择响应结构。
     */

    /**
     * 基于旧请求、实时位置和临时风险重新规划；无可行方案时保存风险并暂停关联任务。
     *
     * @param input 重规划请求，包含原规划、临时风险及可选实时任务状态
     */
    public Map<String,Object> replan(Object input) {
        // p：重规划输入的Map形式。
        var p=map(input);
        // planId：原规划的MySQL主键。
        long planId=number(p.get("plan_id"));
        // original：原规划数据库记录，包含可复用的请求快照。
        var original=store.get("planning_runs",planId);
        // missionId：关联任务的MySQL主键；允许为空的场景表示不关联具体任务。
        Long missionId=p.get("mission_id")==null?null:number(p.get("mission_id"));
        // before：关联任务在计算前的快照；未关联任务时为null。
        Map<String,Object> before=missionId==null?null:store.mapper.find("missions",missionId);
        require(missionId==null || before!=null,409,"任务与原规划不匹配或不处于执行状态");
        if(before!=null) {
            require(number(before.get("plan_id"))==planId && Set.of("running","paused").contains(before.get("status")),409,
                "任务与原规划不匹配或不处于执行状态");
            require(p.get("current_position")!=null,422,"执行中重规划需要当前位置与剩余电量");
        }
        // request：从旧规划解析并按当前位置/电量修正的规划请求。
        var request=objectJson(original.get("request_json"));
        if(p.get("current_position")!=null) {
            require(((List<?>)request.getOrDefault("waypoints",List.of())).isEmpty() || p.get("remaining_waypoints")!=null,
                422,"请明确提供尚未完成的航点");
            if(request.get("home")==null) request.put("home",request.get("start"));
            request.put("start",p.get("current_position"));
            request.put("waypoints",p.getOrDefault("remaining_waypoints",List.of())==null?List.of():p.get("remaining_waypoints"));
            map(request.get("drone")).put("battery_percent",p.get("battery_percent"));
        }
        length(p.get("event_name"),180,"event_name");
        // zoneRequest：由风险事件组装的临时限制区校验请求。
        var zoneRequest=obj("name",p.get("event_name"),"category","temporary_risk","severity",p.get("severity"),"source_name",
            "操作员创建的仿真风险事件","geojson",p.get("geojson"));
        // zone：校验后的待保存限制区字段，已移除计算服务临时ID。
        var zone=new LinkedHashMap<>(store.artifact(store.calculate("restriction",zoneRequest),"restriction"));
        zone.remove("id");
        // context：提供给计算服务的业务快照，键为表名，值为该表记录列表。
        var context=store.context("restriction_zones","data_source_snapshots");
        // zones：计算上下文中的限制区列表副本，稍后追加本次风险。
        var zones=new ArrayList<>(rows(context.get("restriction_zones")));
        // temporary：仅在计算上下文中使用的限制区副本，临时ID避免与已有记录碰撞。
        var temporary=new LinkedHashMap<>(zone);
        temporary.put("id",zones.stream().mapToLong(/* r：遍历已有计算上下文限制区以寻找最大ID的一条记录。 */ r->number(r.get("id"))).max().orElse(0)+1);
        zones.add(temporary);
        context.put("restriction_zones",zones);
        // computed：考虑新风险后的完整规划计算响应。
        Map<String,Object> computed;
        try {
            computed=store.calculate("plan",request,context,null);
        }
        catch(/* e：计算或业务校验抛出的异常，携带HTTP状态码与失败原因。 */ BusinessException e) {
            if(e.getStatus()==409) store.tx(()-> {
                if(missionId!=null) {
                    // locked：重规划失败分支中锁定的任务，用于检查后安全暂停。
                    var locked=store.lock("missions",missionId);
                    checkUnchanged(before,locked);
                    store.update("missions",missionId,obj("status","paused"));
                }
                store.insert("restriction_zones",zone);
                store.event("replan_failed",missionId,obj("reason",e.getMessage(),"plan_id",planId));
                return null;
            });
            throw e;
        }
        return store.tx(()-> {
            if(missionId!=null) checkUnchanged(before,store.lock("missions",missionId));
            // savedZone：成功分支保存到MySQL的限制区，含正式ID。
            var savedZone=store.insert("restriction_zones",zone);
            // plan：新保存的规划响应，随后转换成重规划接口的字段结构。
            var plan=save(computed);
            // newId：新规划的MySQL主键，与旧planId区分。
            long newId=number(plan.get("plan_id"));
            if(missionId!=null) store.update("missions",missionId,obj("plan_id",newId,"route_key",plan.get("recommended_route_key"),
                "drone_json",request.get("drone")));
            store.event("replanned",missionId,obj("old_plan_id",planId,"new_plan_id",newId,"from_live_position",p.get("current_position")!=null));
            plan.remove("plan_id");
            plan.remove("created_at");
            plan.remove("weather");
            plan.putAll(obj("old_plan_id",planId,"new_plan_id",newId,"from_live_position",p.get("current_position")!=null,
                "event",obj("id",savedZone.get("id"),"name",savedZone.get("name"),"severity",savedZone.get("severity"),"geojson",
                parse(savedZone.get("geojson")),"mode","SIMULATION")));
            return plan;
        });
    }

    /**
     * 比较计算前与加锁后的任务状态/规划/航线/更新时间，变化则拒绝覆盖。
     *
     * @param before 耗时计算开始之前读取的任务快照
     * @param current 短事务中加锁后读取的最新任务记录
     */
    public static void checkUnchanged(Map<String,Object> before,Map<String,Object> current) {
        require(Objects.equals(before.get("status"),current.get("status")) && number(before.get("plan_id"))==number(current.get("plan_id"))
            && Objects.equals(before.get("route_key"),current.get("route_key")) && Objects.equals(iso(before.get("updated_at")),
            iso(current.get("updated_at"))),409,"任务已被其他请求修改，请重新查询");
    }
}
