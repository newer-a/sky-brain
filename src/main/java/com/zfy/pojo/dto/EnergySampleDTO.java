package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python EnergySample。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class EnergySampleDTO {
    /** 原字段 flight_id；约束 {"maxLength": 100, "minLength": 1}。 */
    @JsonProperty("flight_id")
    @NotNull
    @Size(min = 1, max = 100)
    private String flightId;

    /** 原字段 effective_distance_km；约束 {"maximum": 200.0, "exclusiveMinimum": 0.0}。 */
    @JsonProperty("effective_distance_km")
    @NotNull
    @DecimalMax(value = "200.0", inclusive = true)
    @DecimalMin(value = "0.0", inclusive = false)
    private Double effectiveDistanceKm;

    /** 原字段 measured_energy_wh；约束 {"maximum": 50000.0, "exclusiveMinimum": 0.0}。 */
    @JsonProperty("measured_energy_wh")
    @NotNull
    @DecimalMax(value = "50000.0", inclusive = true)
    @DecimalMin(value = "0.0", inclusive = false)
    private Double measuredEnergyWh;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (effectiveDistanceKm == null || Double.isFinite(effectiveDistanceKm)) && (measuredEnergyWh == null || Double.isFinite(measuredEnergyWh)); }
}
