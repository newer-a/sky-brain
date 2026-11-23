package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python RestrictionImport。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class RestrictionImportDTO {
    /** 原字段 name；约束 {}。 */
    @JsonProperty("name")
    @NotNull
    private String name;

    /** 原字段 category；约束 {"enum": ["no_fly", "limited", "temporary_risk", "population", "communication"], "default": "no_fly"}。 */
    @JsonProperty("category")
    @NotNull
    @Pattern(regexp = "^(no_fly|limited|temporary_risk|population|communication)$")
    private String category = "no_fly";

    /** 原字段 severity；约束 {"maximum": 100.0, "minimum": 0.0, "default": 100}。 */
    @JsonProperty("severity")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double severity = 100D;

    /** 原字段 min_altitude_m；约束 {"minimum": 0.0, "default": 0}。 */
    @JsonProperty("min_altitude_m")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double minAltitudeM = 0D;

    /** 原字段 max_altitude_m；约束 {"minimum": 0.0, "default": 500}。 */
    @JsonProperty("max_altitude_m")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private Double maxAltitudeM = 500D;

    /** 原字段 source_name；约束 {"minLength": 2}。 */
    @JsonProperty("source_name")
    @NotNull
    @Size(min = 2)
    private String sourceName;

    /** 原字段 source_url；约束 {}。 */
    @JsonProperty("source_url")
    private String sourceUrl;

    /** 原字段 effective_from；约束 {}。 */
    @JsonProperty("effective_from")
    private String effectiveFrom;

    /** 原字段 effective_to；约束 {}。 */
    @JsonProperty("effective_to")
    private String effectiveTo;

    /** 原字段 geojson；约束 {"additionalProperties": true}。 */
    @JsonProperty("geojson")
    @NotNull
    private Map<String, Object> geojson;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (severity == null || Double.isFinite(severity)) && (minAltitudeM == null || Double.isFinite(minAltitudeM)) && (maxAltitudeM == null || Double.isFinite(maxAltitudeM)); }
}
