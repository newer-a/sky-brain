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
 * 飞行日志入口：导入、列表和包含轨迹点的回放详情。
 */
@RestController
@RequiredArgsConstructor
public class ReplayController {
    // records：日志与记录业务服务，负责计算结果归档。
    private final RecordService records;

    /**
     * 计算轨迹指标并保存导入日志。
     *
     * @param body 由请求JSON绑定的ReplayImportDTO，包含日志名称、来源及轨迹点
     */
    @PostMapping("/api/replay/import")
    public LogSummaryVO importReplay(@Valid @RequestBody ReplayImportDTO body) {
        return JSON.convertValue(records.importLog(body), LogSummaryVO.class);
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 返回最近的日志摘要列表。
     */
    @GetMapping("/api/replay")
    public List<LogListItemVO> listLogs() {
        return rows(records.logs()).stream().map(/* row：当前遍历的一条业务结果Map，转换为本接口列表元素的VO。 */ row -> JSON.convertValue(row, LogListItemVO.class)).toList();
    }

    /**
     * 返回指定日志的指标和完整轨迹点。
     *
     * @param log_id URL路径中的本地飞行日志主键，对应flight_logs.id
     */
    @GetMapping("/api/replay/{log_id}")
    public LogDetailVO replay(@PathVariable("log_id") long log_id) {
        return JSON.convertValue(records.log(log_id), LogDetailVO.class);
    }
}
