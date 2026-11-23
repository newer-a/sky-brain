package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DispatchDrone。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DispatchDroneDTO {
    /** 原字段 id；约束 {}。 */
    @JsonProperty("id")
    @NotNull
    private String id;

    /** 原字段 name；约束 {}。 */
    @JsonProperty("name")
    @NotNull
    private String name;

    /** 原字段 battery_percent；约束 {"maximum": 100.0, "minimum": 0.0}。 */
    @JsonProperty("battery_percent")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double batteryPercent;

    /** 原字段 battery_capacity_wh；约束 {"exclusiveMinimum": 20.0}。 */
    @JsonProperty("battery_capacity_wh")
    @NotNull
    @DecimalMin(value = "20.0", inclusive = false)
    private Double batteryCapacityWh;

    /** 原字段 energy_wh_per_km；约束 {"exclusiveMinimum": 1.0}。 */
    @JsonProperty("energy_wh_per_km")
    @NotNull
    @DecimalMin(value = "1.0", inclusive = false)
    private Double energyWhPerKm;

    /** 原字段 cruise_speed_mps；约束 {"exclusiveMinimum": 1.0}。 */
    @JsonProperty("cruise_speed_mps")
    @NotNull
    @DecimalMin(value = "1.0", inclusive = false)
    private Double cruiseSpeedMps;

    /** 原字段 available_at_s；约束 {"minimum": 0.0, "default": 0}。 */
    @JsonProperty("available_at_s")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double availableAtS = 0D;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (batteryPercent == null || Double.isFinite(batteryPercent)) && (batteryCapacityWh == null || Double.isFinite(batteryCapacityWh)) && (energyWhPerKm == null || Double.isFinite(energyWhPerKm)) && (cruiseSpeedMps == null || Double.isFinite(cruiseSpeedMps)) && (availableAtS == null || Double.isFinite(availableAtS)); }
}
