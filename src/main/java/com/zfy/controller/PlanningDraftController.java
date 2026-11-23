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
 * 局部类型推断：var让编译器根据右侧表达式推断类型，并不是动态类型。
 * 例如var row = store.get(...)推断为Map<String,Object>，之后不能把row改为不相关类型。
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
 * 规划草案入口：生成、查询和采用草案。
 */
@RestController
@RequiredArgsConstructor
public class PlanningDraftController {
    // agents：AI业务服务，负责问答、草案和分析记录。
    private final AgentService agents;

    /**
     * 根据自然语言生成并保存规划草案。
     *
     * @param body 由请求JSON绑定的DraftDTO，包含生成草案的自然语言需求及offline/llm模式
     */
    @PostMapping("/api/ai/planning/draft")
    public DraftVO planningDraft(@Valid @RequestBody DraftDTO body) {
        return JSON.convertValue(agents.draft(body), DraftVO.class);
    }

    /*
     * Stream流水线：stream()把集合变为流，filter筛选，map把每项转换成另一种值，limit限制数量。
     * row/r -> 表达式是Lambda，箭头左侧是当前元素，右侧是处理结果；它不是新线程。
     * toList收集为列表，findFirst取第一项；这些终止操作触发流水线执行，Stream本身不会自动查询数据库。
     */

    /**
     * 返回最近的草案摘要。
     */
    @GetMapping("/api/ai/planning/drafts")
    public List<DraftSummaryVO> drafts() {
        return rows(agents.drafts()).stream().map(/* row：当前遍历的一条业务结果Map，转换为本接口列表元素的VO。 */ row -> JSON.convertValue(row, DraftSummaryVO.class)).toList();
    }

    /**
     * 重新校验并采用草案；已经采用时返回原规划。
     *
     * @param draft_id URL路径中的草案主键，对应planning_drafts.id
     */
    @PostMapping("/api/ai/planning/drafts/{draft_id}/apply")
    public Object applyDraft(@PathVariable("draft_id") long draft_id) {
        // result：采用草案的结果，含新规划或already_applied重复标记。
        var result = map(agents.applyDraft(draft_id));
        if (yes(result.get("already_applied"))) return JSON.convertValue(result, DraftAlreadyAppliedVO.class);
        return JSON.convertValue(result, DraftAppliedVO.class);
    }
}
