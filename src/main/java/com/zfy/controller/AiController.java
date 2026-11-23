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
 * AI状态和业务问答入口；业务数据与失败记录交给AgentService处理。
 */
@RestController
@RequiredArgsConstructor
public class AiController {
    // agents：AI业务服务，负责问答、草案和分析记录。
    private final AgentService agents;

    /**
     * 查询计算服务侧的AI配置状态。
     */
    @GetMapping("/api/ai/status")
    public AiStatusVO aiStatus() {
        return JSON.convertValue(agents.status(), AiStatusVO.class);
    }

    /**
     * 将业务问题交给Agent，并返回回答、依据和调用结果。
     *
     * @param body 由请求JSON绑定的AICommandDTO，包含用户业务问题及问答参数
     */
    @PostMapping("/api/ai/command")
    public AiCommandVO aiCommand(@Valid @RequestBody AICommandDTO body) {
        return JSON.convertValue(agents.command(body), AiCommandVO.class);
    }
}
