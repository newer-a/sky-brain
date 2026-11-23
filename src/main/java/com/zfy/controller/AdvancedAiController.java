package com.zfy.controller;

import com.zfy.pojo.dto.*;
import com.zfy.pojo.vo.*;
import com.zfy.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.zfy.service.JsonSupport.*;

/*
 * Spring入口注解：@RestController让返回对象自动序列化为JSON；Mapping注解把URL映射到方法。
 * @RequestBody把JSON绑定到DTO，@Valid触发DTO字段校验；@PathVariable读取路径中的ID。
 */

/*
 * Lombok构造器注入：@RequiredArgsConstructor为尚未初始化的final字段生成构造器，Spring通过构造器传入Service。
 * final只限制字段引用不再重新赋值，不会让Service对象本身变成不可修改。
 */

/*
 * 泛型阅读：Map<String,Object>的键是字段名，值可为数字、文本、列表或嵌套对象。
 * List<X>表示元素类型为X的列表；Object保留灵活性，但取值时必须遵守约定的数据结构。
 */

/*
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * 类字面量：PlanVO.class等表达式表示目标类型的信息，不是创建一个VO实例。
 * JSON.convertValue(结果, 某VO.class)按字段把Service结果转换为VO，再由Spring输出JSON。
 */

/**
 * 高级AI入口：生成/采用机队方案、诊断飞行日志、查询分析历史。
 */
@RestController
@RequiredArgsConstructor
public class AdvancedAiController {
    // agents：AI业务服务，负责问答、草案和分析记录。
    private final AgentService agents;

    /**
     * 接收机队需求，返回AI协同方案草案。
     *
     * @param body 由请求JSON绑定的FleetDTO，包含机队协同需求、任务信息及约束
     */
    @PostMapping("/api/ai/advanced/fleet")
    public FleetDraftVO proposeFleet(@Valid @RequestBody FleetDTO body) {
        return JSON.convertValue(agents.fleet(body), FleetDraftVO.class);
    }

    /**
     * 采用指定分析事件中的机队方案。
     *
     * @param analysis_id URL路径中的分析事件主键，对应operation_events.id
     */
    @PostMapping("/api/ai/advanced/fleet/{analysis_id}/apply")
    public FleetAppliedVO applyFleet(@PathVariable("analysis_id") long analysis_id) {
        return JSON.convertValue(agents.applyFleet(analysis_id), FleetAppliedVO.class);
    }

    /**
     * 根据指定日志及问题生成飞行诊断。
     *
     * @param body 由请求JSON绑定的DiagnosisDTO，包含需要诊断的日志及分析问题
     */
    @PostMapping("/api/ai/advanced/diagnosis")
    public DiagnosisVO diagnoseFlight(@Valid @RequestBody DiagnosisDTO body) {
        return JSON.convertValue(agents.diagnosis(body), DiagnosisVO.class);
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 返回最近的机队/诊断分析历史列表。
     */
    @GetMapping("/api/ai/advanced/history")
    public List<AnalysisHistoryItemVO> history() {
        return rows(agents.history()).stream().map(/* row：当前遍历的一条业务结果Map，转换为本接口列表元素的VO。 */ row -> JSON.convertValue(row, AnalysisHistoryItemVO.class)).toList();
    }
}
