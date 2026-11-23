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
 * 限制区入口：导入限制区、查询当前生效的限制区。
 */
@RestController
@RequiredArgsConstructor
public class RestrictionController {
    // planning：规划业务服务，负责环境、限制区和规划快照。
    private final PlanningService planning;

    /**
     * 校验并保存用户导入的限制区。
     *
     * @param body 由请求JSON绑定的RestrictionImportDTO，包含限制区名称、范围、高度、有效期及来源
     */
    @PostMapping("/api/restrictions/import")
    public RestrictionCreatedVO importRestriction(@Valid @RequestBody RestrictionImportDTO body) {
        return JSON.convertValue(planning.importRestriction(body), RestrictionCreatedVO.class);
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 返回当前处于有效期内的限制区。
     */
    @GetMapping("/api/restrictions")
    public List<RestrictionVO> listRestrictions() {
        return rows(planning.restrictions()).stream().map(/* row：当前遍历的一条业务结果Map，转换为本接口列表元素的VO。 */ row -> JSON.convertValue(row, RestrictionVO.class)).toList();
    }
}
