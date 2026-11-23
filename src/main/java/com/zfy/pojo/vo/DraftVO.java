package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 DraftVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class DraftVO {
    @JsonProperty("draft_id")
    private Long draftId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("model")
    private String model;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("request")
    private PlanDTO request;

    @JsonProperty("strategy")
    private StrategyVO strategy;

    @JsonProperty("metrics")
    private PlanningMetricsVO metrics;

    @JsonProperty("changes")
    private List<DraftChangeVO> changes;

    @JsonProperty("tool_trace")
    private List<ToolTraceVO> toolTrace;

    @JsonProperty("alternatives")
    private List<StrategyAlternativeVO> alternatives;

    @JsonProperty("preview")
    private PlanningResultVO preview;

    @JsonProperty("rules")
    private List<RuleVO> rules;

    @JsonProperty("sources")
    private List<SourceMetaVO> sources;

}
