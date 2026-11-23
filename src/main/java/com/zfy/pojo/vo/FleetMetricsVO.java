package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 FleetMetricsVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class FleetMetricsVO {
    @JsonProperty("scheduled")
    private Integer scheduled;

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("makespan_s")
    private Double makespanS;

    @JsonProperty("energy_wh")
    private Double energyWh;

    @JsonProperty("unresolved_conflicts")
    private Integer unresolvedConflicts;

    @JsonProperty("minimum_separation_m")
    private Double minimumSeparationM;

    @JsonProperty("minimum_remaining_battery_percent")
    private Double minimumRemainingBatteryPercent;

}
