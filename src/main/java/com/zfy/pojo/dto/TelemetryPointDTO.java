package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python TelemetryPoint。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class TelemetryPointDTO {
    /** 原字段 t_s；约束 {"minimum": 0.0}。 */
    @JsonProperty("t_s")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double tS;

    /** 原字段 longitude；约束 {"maximum": 180.0, "minimum": -180.0}。 */
    @JsonProperty("longitude")
    @NotNull
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    private Double longitude;

    /** 原字段 latitude；约束 {"maximum": 90.0, "minimum": -90.0}。 */
    @JsonProperty("latitude")
    @NotNull
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    private Double latitude;

    /** 原字段 altitude_m；约束 {"maximum": 10000.0, "minimum": -500.0}。 */
    @JsonProperty("altitude_m")
    @NotNull
    @DecimalMin(value = "-500.0", inclusive = true)
    @DecimalMax(value = "10000.0", inclusive = true)
    private Double altitudeM;

    /** 原字段 battery_percent；约束 {"maximum": 100.0, "minimum": 0.0}。 */
    @JsonProperty("battery_percent")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double batteryPercent;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (tS == null || Double.isFinite(tS)) && (longitude == null || Double.isFinite(longitude)) && (latitude == null || Double.isFinite(latitude)) && (altitudeM == null || Double.isFinite(altitudeM)) && (batteryPercent == null || Double.isFinite(batteryPercent)); }
}
