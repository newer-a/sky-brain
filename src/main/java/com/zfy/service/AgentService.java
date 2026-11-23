package com.zfy.service;

import com.zfy.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import static com.zfy.service.JsonSupport.*;
import static com.zfy.service.BusinessStore.*;

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
 * 组织AI计算与业务落库；模型调用在计算服务中，正式草案和事件保存在MySQL。
 */
@Service
public class AgentService {
    // store：业务存储组件，统一处理MySQL读写、事务和计算上下文。
    private final BusinessStore store;
    // planning：规划业务服务，负责环境、限制区和规划快照。
    private final PlanningService planning;

    /**
     * 构造本服务，接收Spring注入的依赖并保存到字段。
     *
     * @param store 业务存储组件，统一处理MySQL读写、事务和计算上下文
     * @param planning 规划业务服务，负责环境、限制区和规划快照
     */
    public AgentService(BusinessStore store,PlanningService planning) {
        this.store=store;
        this.planning=planning;
    }

    /**
     * 查询计算服务中的AI状态。
     */
    public Object status() {
        return store.calculate("ai-status",obj()).get("data");
    }

    /*
     * 事务Lambda：() -> {...}是无参回调，交给store.tx执行；大括号内的return是回调的返回值。
     * 先完成网络计算，再把关联写库放入短事务，减少长期持有数据库锁的时间。
     */

    /**
     * 用MySQL快照进行业务问答；模型不可用时保存失败摘要。
     *
     * @param payload 用户问答请求，含问题及工具调用相关参数
     */
    public Object command(Object payload) {
        // Agent仅使用Java提供的MySQL业务快照，不读取旧SQLite业务数据。
        // result：问答响应Map，包含可用性、答案及失败原因。
        var result=map(store.calculate("ai-command",payload,"missions","planning_runs","restriction_zones","operation_events").get("data"));
        if(!yes(result.get("available"))) store.tx(()->{
            store.event("ai_operation_failed",null,obj("operation","ai-command","status",result.get("status"),"reason",result.get("reason")));
            return null;
        });
        return result;
    }

    /**
     * 生成草案并保存到planning_drafts，替换计算服务的临时ID。
     *
     * @param payload 草案生成请求，包含message和mode等字段
     */
    public Object draft(Object payload) {
        // p：草案请求转换成的Map，用来读取模式和自然语言需求。
        var p=map(payload);
        // result：计算生成的草案内容，保存时去除临时ID，再回填正式ID。
        var result=map(store.calculate("draft",payload,"restriction_zones","data_source_snapshots").get("data"));
        return store.tx(()-> {
            result.remove("draft_id");
            // row：插入planning_drafts后读取的完整数据库记录。
            var row=store.insert("planning_drafts",obj("mode",p.get("mode"),"status","draft","prompt",p.get("message"),"payload_json",
                result));
            // id：MySQL生成的草案主键。
            long id=number(row.get("id"));
            result.put("draft_id",id);
            result.put("status","draft");
            store.event("planning_draft_created",null,obj("draft_id",id,"mode",p.get("mode"),"strategy",result.get("strategy")));
            return result;
        });
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 查询最近30条草案并转换为列表字段。
     */
    public Object drafts() {
        return store.list("planning_drafts").stream().limit(30).map(/* r：当前遍历的草案数据库记录。 */ r->obj("id",r.get("id"),"mode",r.get("mode"),"status",
            r.get("status"),"prompt",r.get("prompt"),"plan_id",r.get("applied_plan_id"),"created_at",iso(r.get("created_at")))).toList();
    }

    /*
     * 条件更新的返回值：changeDraft返回影响行数，等于1说明旧状态匹配并且本次成功改为applying。
     * 这里先占用草案，再在事务外做耗时规划，最后在短事务中保存正式规划。
     * catch中的恢复针对普通运行时异常；进程直接崩溃时不会执行catch，所以还没有自动恢复租约。
     */

    /**
     * 校验草案时效，用条件更新防止重复采用，重新规划后保存正式规划；普通异常时恢复draft状态。
     *
     * @param id 要采用的草案主键
     */
    public Object applyDraft(long id) {
        // row：采用前读取的草案记录，用于检查状态和创建时间。
        var row=store.get("planning_drafts",id);
        if("applied".equals(row.get("status"))) return obj("draft_id",id,"already_applied",true,"plan",planning.get(number(row.get("applied_plan_id"))));
        require(Duration.between(time(row.get("created_at")),now()).toHours()<24,409,"草案已超过24小时，请重新生成");
        require(store.mapper.changeDraft(id,"draft","applying")==1,409,"该草案正在采用，请稍后查询");
        try {
            // request：从草案payload_json中取出的结构化规划请求。
            var request=objectJson(row.get("payload_json")).get("request");
            // computed：重新校验后计算出的规划响应，尚未保存为正式规划。
            var computed=store.calculate("plan",request,"restriction_zones","data_source_snapshots");
            return store.tx(()-> {
                // current：加锁后读取的草案记录，用于确认状态仍为applying。
                var current=store.lock("planning_drafts",id);
                require("applying".equals(current.get("status")),409,"草案状态已改变");
                // plan：已保存的正式规划响应，包含MySQL生成的plan_id。
                var plan=planning.save(computed);
                store.update("planning_drafts",id,obj("status","applied","applied_plan_id",plan.get("plan_id")));
                store.event("planning_draft_applied",null,obj("draft_id",id,"plan_id",plan.get("plan_id"),"revalidated",true));
                return obj("draft_id",id,"request",request,"plan",plan);
            });
        } catch(/* e：当前操作抛出的运行时异常。 */ RuntimeException e) {
            store.mapper.changeDraft(id,"applying","draft");
            throw e;
        }
    }

    /**
     * 生成机队协同草案，并将分析结果保存为操作事件。
     *
     * @param input 机队协同方案生成请求
     */
    public Object fleet(Object input) {
        // result：计算出的机队分析结果，稍后回填正式analysis_id。
        var result=map(store.calculate("fleet",input,"planning_runs","restriction_zones").get("data"));
        result.remove("analysis_id");
        return store.tx(()->{
            // id：保存机队草案事件后生成的MySQL事件主键。
            long id=store.event("ai_fleet_draft",null,result);
            result.put("analysis_id",id);
            return result;
        });
    }

    /**
     * 检查协同方案类型/时效，执行采用计算，再加锁校验并更新分析事件。
     *
     * @param id 要采用的机队分析事件主键
     */
    public Object applyFleet(long id) {
        // before：计算前读取的分析事件快照，用于时效和并发校验。
        var before=store.get("operation_events",id);
        require(Set.of("ai_fleet_draft","ai_fleet_applied").contains(before.get("kind")),404,"协同方案不存在");
        require(Duration.between(time(before.get("created_at")),now()).toHours()<24,409,"协同方案已过期");
        // context：提供给计算服务的业务快照，键为表名，值为该表记录列表。
        var context=store.context("planning_runs","restriction_zones");
        // event：待传给计算服务的事件副本，将创建时间转换为ISO字符串。
        var event=new LinkedHashMap<>(before);
        event.put("created_at",iso(before.get("created_at")));
        context.put("operation_events",List.of(event));
        // response：采用机队方案后的完整计算响应。
        var response=store.calculate("fleet-apply",obj(),context,id);
        return store.tx(()-> {
            // current：加锁后读取的分析事件，用于与计算前详情比较。
            var current=store.lock("operation_events",id);
            require(Objects.equals(before.get("detail_json"),current.get("detail_json")),409,"协同方案已被修改，请重新查询");
            // artifact：计算服务返回的更新后事件产物，包含新的detail_json。
            var artifact=store.artifact(response,"event");
            store.update("operation_events",id,obj("kind","ai_fleet_applied","detail_json",artifact.get("detail_json")));
            return response.get("data");
        });
    }

    /**
     * 生成飞行诊断并保存分析事件，返回MySQL正式事件ID。
     *
     * @param input 飞行诊断请求，包含日志选择和分析问题
     */
    public Object diagnosis(Object input) {
        // result：诊断结果Map，保存后回填正式analysis_id。
        var result=map(store.calculate("diagnosis",input,"flight_logs").get("data"));
        result.remove("analysis_id");
        return store.tx(()->{
            // id：保存飞行诊断事件后生成的MySQL事件主键。
            long id=store.event("ai_flight_diagnosis",null,result);
            result.put("analysis_id",id);
            return result;
        });
    }

    /**
     * 查询最近20条机队方案和飞行诊断的摘要。
     */
    public Object history() {
        return store.list("operation_events").stream().filter(/* r：当前遍历的操作事件，只保留机队方案与诊断类型。 */ r->Set.of("ai_fleet_draft","ai_fleet_applied","ai_flight_diagnosis").contains(r.get("kind"))).limit(20)
        .map(/* r：当前遍历的操作事件，只保留机队方案与诊断类型。 */ r->obj("id",r.get("id"),"kind",r.get("kind"),"created_at",iso(r.get("created_at")),"summary",objectJson(r.get("detail_json")).getOrDefault("summary",
            ""))).toList();
    }
}
