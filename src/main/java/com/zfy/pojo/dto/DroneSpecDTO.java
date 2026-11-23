package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DroneSpec。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DroneSpecDTO {
    /** 原字段 energy_model；约束 {"enum": ["engineering", "cmu_m100"], "default": "engineering"}。 */
    @JsonProperty("energy_model")
    @NotNull
    @Pattern(regexp = "^(engineering|cmu_m100)$")
    private String energyModel = "engineering";

    /** 原字段 name；约束 {"default": "工业巡检多旋翼"}。 */
    @JsonProperty("name")
    @NotNull
    private String name = "工业巡检多旋翼";

    /** 原字段 cruise_speed_mps；约束 {"maximum": 40.0, "exclusiveMinimum": 1.0, "default": 12}。 */
    @JsonProperty("cruise_speed_mps")
    @NotNull
    @DecimalMax(value = "40.0", inclusive = true)
    @DecimalMin(value = "1.0", inclusive = false)
    private Double cruiseSpeedMps = 12D;

    /** 原字段 battery_capacity_wh；约束 {"exclusiveMinimum": 20.0, "default": 600}。 */
    @JsonProperty("battery_capacity_wh")
    @NotNull
    @DecimalMin(value = "20.0", inclusive = false)
    private Double batteryCapacityWh = 600D;

    /** 原字段 energy_wh_per_km；约束 {"exclusiveMinimum": 1.0, "default": 24}。 */
    @JsonProperty("energy_wh_per_km")
    @NotNull
    @DecimalMin(value = "1.0", inclusive = false)
    private Double energyWhPerKm = 24D;

    /** 原字段 max_wind_mps；约束 {"exclusiveMinimum": 1.0, "default": 12}。 */
    @JsonProperty("max_wind_mps")
    @NotNull
    @DecimalMin(value = "1.0", inclusive = false)
    private Double maxWindMps = 12D;

    /** 原字段 battery_percent；约束 {"maximum": 100.0, "minimum": 0.0, "default": 86}。 */
    @JsonProperty("battery_percent")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double batteryPercent = 86D;

    /** 原字段 payload_kg；约束 {"maximum": 50.0, "minimum": 0.0, "default": 1.2}。 */
    @JsonProperty("payload_kg")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "50.0", inclusive = true)
    private Double payloadKg = 1.2D;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (cruiseSpeedMps == null || Double.isFinite(cruiseSpeedMps)) && (batteryCapacityWh == null || Double.isFinite(batteryCapacityWh)) && (energyWhPerKm == null || Double.isFinite(energyWhPerKm)) && (maxWindMps == null || Double.isFinite(maxWindMps)) && (batteryPercent == null || Double.isFinite(batteryPercent)) && (payloadKg == null || Double.isFinite(payloadKg)); }
}
