package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 PlanningMetricsVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class PlanningMetricsVO {
    @JsonProperty("key")
    private String key;

    @JsonProperty("label")
    private String label;

    @JsonProperty("distance_m")
    private Double distanceM;

    @JsonProperty("duration_s")
    private Double durationS;

    @JsonProperty("risk_score")
    private Double riskScore;

    @JsonProperty("energy_wh")
    private Double energyWh;

    @JsonProperty("return_energy_wh")
    private Double returnEnergyWh;

    @JsonProperty("after_return_battery_percent")
    private Double afterReturnBatteryPercent;

    @JsonProperty("data_completeness_percent")
    private Double dataCompletenessPercent;

    @JsonProperty("feasible")
    private Boolean feasible;

}
