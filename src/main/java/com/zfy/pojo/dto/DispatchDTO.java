package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DispatchRequest。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DispatchDTO {
    /** 原字段 drones；约束 {"minItems": 1}。 */
    @JsonProperty("drones")
    @NotNull
    @Valid
    @Size(min = 1)
    private List<@NotNull DispatchDroneDTO> drones;

    /** 原字段 jobs；约束 {"minItems": 1}。 */
    @JsonProperty("jobs")
    @NotNull
    @Valid
    @Size(min = 1)
    private List<@NotNull DispatchJobDTO> jobs;

    /** 原字段 reserve_percent；约束 {"maximum": 80.0, "minimum": 5.0, "default": 25}。 */
    @JsonProperty("reserve_percent")
    @NotNull
    @DecimalMin(value = "5.0", inclusive = true)
    @DecimalMax(value = "80.0", inclusive = true)
    private Double reservePercent = 25D;

    /** 原字段 separation_m；约束 {"maximum": 300.0, "minimum": 5.0, "default": 30}。 */
    @JsonProperty("separation_m")
    @NotNull
    @DecimalMin(value = "5.0", inclusive = true)
    @DecimalMax(value = "300.0", inclusive = true)
    private Double separationM = 30D;

    /** 原字段 max_delay_s；约束 {"maximum": 3600.0, "minimum": 0.0, "default": 900}。 */
    @JsonProperty("max_delay_s")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "3600.0", inclusive = true)
    private Integer maxDelayS = 900;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (reservePercent == null || Double.isFinite(reservePercent)) && (separationM == null || Double.isFinite(separationM)); }

    @JsonIgnore
    @AssertTrue(message = "跨字段约束未满足，详见原接口文档")
    public boolean isConsistent() { if(drones==null || jobs==null) return true; if(drones.stream().anyMatch(Objects::isNull) || jobs.stream().anyMatch(Objects::isNull)) return false; return drones.stream().map(DispatchDroneDTO::getId).distinct().count()==drones.size() && jobs.stream().map(DispatchJobDTO::getId).distinct().count()==jobs.size() && jobs.stream().allMatch(j->j.getCoordinates()==null || j.getCoordinates().size()!=1); }
}
