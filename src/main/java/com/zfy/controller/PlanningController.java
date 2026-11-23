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
 * 静态导入：import static允许直接写obj(...)、map(...)、require(...)等工具方法。
 * 调用时省略的是工具类名，不是省略了一层业务逻辑；具体实现见对应静态工具类。
 */

/*
 * 类字面量：PlanVO.class等表达式表示目标类型的信息，不是创建一个VO实例。
 * JSON.convertValue(结果, 某VO.class)按字段把Service结果转换为VO，再由Spring输出JSON。
 */

/**
 * 航线规划入口：新建规划、读取快照、发生风险后重新规划。
 */
@RestController
@RequiredArgsConstructor
public class PlanningController {
    // planning：规划业务服务，负责环境、限制区和规划快照。
    private final PlanningService planning;

    /**
     * 计算航线并保存规划快照。
     *
     * @param body 由请求JSON绑定的PlanDTO，包含起终点、航点、无人机及规划约束
     */
    @PostMapping("/api/plan")
    public PlanVO createPlan(@Valid @RequestBody PlanDTO body) {
        return JSON.convertValue(planning.create(body), PlanVO.class);
    }

    /**
     * 根据规划ID返回详情及原始请求。
     *
     * @param plan_id URL路径中的规划记录主键，对应planning_runs.id
     */
    @GetMapping("/api/plans/{plan_id}")
    public PlanDetailVO getPlan(@PathVariable("plan_id") long plan_id) {
        return JSON.convertValue(planning.get(plan_id), PlanDetailVO.class);
    }

    /**
     * 导入临时风险并重新规划，必要时更新关联任务。
     *
     * @param body 由请求JSON绑定的ReplanDTO，包含旧规划、可选任务、实时状态和新增风险区域
     */
    @PostMapping("/api/replan")
    public ReplanVO replan(@Valid @RequestBody ReplanDTO body) {
        return JSON.convertValue(planning.replan(body), ReplanVO.class);
    }
}
