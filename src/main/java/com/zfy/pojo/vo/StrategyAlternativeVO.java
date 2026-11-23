package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 StrategyAlternativeVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyAlternativeVO {
    @JsonProperty("strategy")
    private StrategyVO strategy;

    @JsonProperty("metrics")
    private PlanningMetricsVO metrics;

    @JsonProperty("error")
    private String error;

}
