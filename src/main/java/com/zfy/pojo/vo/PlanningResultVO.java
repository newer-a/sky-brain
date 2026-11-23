package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 PlanningResultVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class PlanningResultVO {
    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("recommended_route_key")
    private String recommendedRouteKey;

    @JsonProperty("routes")
    private List<RouteVO> routes;

    @JsonProperty("rejected_profiles")
    private List<RejectedProfileVO> rejectedProfiles;

    @JsonProperty("objective")
    private String objective;

    @JsonProperty("return_to_start")
    private Boolean returnToStart;

    @JsonProperty("validation")
    private RouteValidationVO validation;

    @JsonProperty("risk_grid")
    private List<RiskCellVO> riskGrid;

    @JsonProperty("bbox")
    private List<Double> bbox;

    @JsonProperty("source_availability")
    private Map<String,Boolean> sourceAvailability;

    @JsonProperty("compute_ms")
    private Double computeMs;

}
