package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DispatchJob。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DispatchJobDTO {
    /** 原字段 id；约束 {}。 */
    @JsonProperty("id")
    @NotNull
    private String id;

    /** 原字段 name；约束 {}。 */
    @JsonProperty("name")
    @NotNull
    private String name;

    /** 原字段 distance_m；约束 {"exclusiveMinimum": 0.0}。 */
    @JsonProperty("distance_m")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double distanceM;

    /** 原字段 priority；约束 {"maximum": 5.0, "minimum": 1.0, "default": 1}。 */
    @JsonProperty("priority")
    @NotNull
    @DecimalMin(value = "1.0", inclusive = true)
    @DecimalMax(value = "5.0", inclusive = true)
    private Integer priority = 1;

    /** 原字段 earliest_start_s；约束 {"minimum": 0.0, "default": 0}。 */
    @JsonProperty("earliest_start_s")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double earliestStartS = 0D;

    /** 原字段 coordinates；约束 {"maxItems": 200}。 */
    @JsonProperty("coordinates")
    @NotNull
    @Valid
    @Size(max = 200)
    private List<@NotNull CoordinateDTO> coordinates = new ArrayList<>();

    /** 原字段 energy_factor；约束 {"maximum": 3.0, "minimum": 0.5, "default": 1}。 */
    @JsonProperty("energy_factor")
    @NotNull
    @DecimalMin(value = "0.5", inclusive = true)
    @DecimalMax(value = "3.0", inclusive = true)
    private Double energyFactor = 1D;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (distanceM == null || Double.isFinite(distanceM)) && (earliestStartS == null || Double.isFinite(earliestStartS)) && (energyFactor == null || Double.isFinite(energyFactor)); }
}
