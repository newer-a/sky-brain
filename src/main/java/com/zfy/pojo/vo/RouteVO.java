package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 RouteVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class RouteVO {
    @JsonProperty("key")
    private String key;

    @JsonProperty("label")
    private String label;

    @JsonProperty("color")
    private String color;

    @JsonProperty("energy_model")
    private String energyModel;

    @JsonProperty("coordinates")
    private List<MapPointVO> coordinates;

    @JsonProperty("return_coordinates")
    private List<MapPointVO> returnCoordinates;

    @JsonProperty("altitude_reference")
    private String altitudeReference;

    @JsonProperty("agl_m")
    private Double aglM;

    @JsonProperty("distance_m")
    private Double distanceM;

    @JsonProperty("return_distance_m")
    private Double returnDistanceM;

    @JsonProperty("duration_s")
    private Double durationS;

    @JsonProperty("energy_wh")
    private Double energyWh;

    @JsonProperty("return_energy_wh")
    private Double returnEnergyWh;

    @JsonProperty("battery_used_percent")
    private Double batteryUsedPercent;

    @JsonProperty("remaining_battery_percent")
    private Double remainingBatteryPercent;

    @JsonProperty("after_return_battery_percent")
    private Double afterReturnBatteryPercent;

    @JsonProperty("reserve_satisfied")
    private Boolean reserveSatisfied;

    @JsonProperty("feasible")
    private Boolean feasible;

    @JsonProperty("rejection_reasons")
    private List<String> rejectionReasons;

    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("risk_components")
    private Map<String,Double> riskComponents;

    @JsonProperty("data_completeness_percent")
    private Double dataCompletenessPercent;

    @JsonProperty("safety_margin_m")
    private Double safetyMarginM;

    @JsonProperty("node_count")
    private Integer nodeCount;

    @JsonProperty("profiles")
    private List<RouteProfileVO> profiles;

    @JsonProperty("recommendation_score")
    private Double recommendationScore;

}
