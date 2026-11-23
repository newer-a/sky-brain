package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python CalibrationRequest。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class CalibrationDTO {
    /** 原字段 samples；约束 {"maxItems": 10000, "minItems": 8}。 */
    @JsonProperty("samples")
    @NotNull
    @Valid
    @Size(min = 8, max = 10000)
    private List<@NotNull EnergySampleDTO> samples;

    /** 原字段 baseline_wh_per_km；约束 {"maximum": 500.0, "exclusiveMinimum": 0.0, "default": 24}。 */
    @JsonProperty("baseline_wh_per_km")
    @NotNull
    @DecimalMax(value = "500.0", inclusive = true)
    @DecimalMin(value = "0.0", inclusive = false)
    private Double baselineWhPerKm = 24D;

    /** 原字段 source_name；约束 {"maxLength": 200, "minLength": 2}。 */
    @JsonProperty("source_name")
    @NotNull
    @Size(min = 2, max = 200)
    private String sourceName;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (baselineWhPerKm == null || Double.isFinite(baselineWhPerKm)); }

    @JsonIgnore
    @AssertTrue(message = "跨字段约束未满足，详见原接口文档")
    public boolean isConsistent() { return samples == null || (samples.stream().noneMatch(Objects::isNull) && samples.stream().map(EnergySampleDTO::getFlightId).distinct().count() == samples.size()); }
}
