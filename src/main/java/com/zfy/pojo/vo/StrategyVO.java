package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 StrategyVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class StrategyVO {
    @JsonProperty("objective")
    private String objective;

    @JsonProperty("altitude_m")
    private Double altitudeM;

    @JsonProperty("reserve_percent")
    private Double reservePercent;

    @JsonProperty("max_risk_score")
    private Double maxRiskScore;

    @JsonProperty("return_to_start")
    private Boolean returnToStart;

}
