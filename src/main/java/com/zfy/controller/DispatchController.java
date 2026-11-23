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
 * 机队任务调度优化入口。
 */
@RestController
@RequiredArgsConstructor
public class DispatchController {
    // records：日志与记录业务服务，负责计算结果归档。
    private final RecordService records;

    /**
     * 计算任务与机队之间的调度分配方案。
     *
     * @param body 由请求JSON绑定的DispatchDTO，包含待分配任务、机队及调度约束
     */
    @PostMapping("/api/dispatch/optimize")
    public DispatchResultVO optimizeDispatch(@Valid @RequestBody DispatchDTO body) {
        return JSON.convertValue(records.calculate("dispatch",body,null), DispatchResultVO.class);
    }
}
