package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python Coordinate。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class CoordinateDTO {
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

    /** 原字段 altitude_m；约束 {"maximum": 500.0, "minimum": 20.0, "default": 120}。 */
    @JsonProperty("altitude_m")
    @NotNull
    @DecimalMin(value = "20.0", inclusive = true)
    @DecimalMax(value = "500.0", inclusive = true)
    private Double altitudeM = 120D;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (longitude == null || Double.isFinite(longitude)) && (latitude == null || Double.isFinite(latitude)) && (altitudeM == null || Double.isFinite(altitudeM)); }
}
