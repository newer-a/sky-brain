package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python PlanRequest。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class PlanDTO {
    /** 原字段 start；约束 {}。 */
    @JsonProperty("start")
    @NotNull
    @Valid
    private CoordinateDTO start;

    /** 原字段 end；约束 {}。 */
    @JsonProperty("end")
    @NotNull
    @Valid
    private CoordinateDTO end;

    /** 原字段 waypoints；约束 {"maxItems": 10}。 */
    @JsonProperty("waypoints")
    @NotNull
    @Valid
    @Size(max = 10)
    private List<@NotNull CoordinateDTO> waypoints = new ArrayList<>();

    /** 原字段 drone；约束 {}。 */
    @JsonProperty("drone")
    @NotNull
    @Valid
    private DroneSpecDTO drone = new DroneSpecDTO();

    /** 原字段 reserve_battery_percent；约束 {"maximum": 80.0, "minimum": 5.0, "default": 25}。 */
    @JsonProperty("reserve_battery_percent")
    @NotNull
    @DecimalMin(value = "5.0", inclusive = true)
    @DecimalMax(value = "80.0", inclusive = true)
    private Double reserveBatteryPercent = 25D;

    /** 原字段 task_type；约束 {"enum": ["inspection", "rescue", "logistics", "patrol"], "default": "inspection"}。 */
    @JsonProperty("task_type")
    @NotNull
    @Pattern(regexp = "^(inspection|rescue|logistics|patrol)$")
    private String taskType = "inspection";

    /** 原字段 grid_size；约束 {"maximum": 60.0, "minimum": 20.0, "default": 34}。 */
    @JsonProperty("grid_size")
    @NotNull
    @DecimalMin(value = "20.0", inclusive = true)
    @DecimalMax(value = "60.0", inclusive = true)
    private Integer gridSize = 34;

    /** 原字段 data_mode；约束 {"enum": ["auto", "real", "simulation"], "default": "auto"}。 */
    @JsonProperty("data_mode")
    @NotNull
    @Pattern(regexp = "^(auto|real|simulation)$")
    private String dataMode = "auto";

    /** 原字段 objective；约束 {"enum": ["shortest", "safest", "energy", "balanced"], "default": "balanced"}。 */
    @JsonProperty("objective")
    @NotNull
    @Pattern(regexp = "^(shortest|safest|energy|balanced)$")
    private String objective = "balanced";

    /** 原字段 return_to_start；约束 {"default": true}。 */
    @JsonProperty("return_to_start")
    @NotNull
    private Boolean returnToStart = true;

    /** 原字段 home；约束 {}。 */
    @JsonProperty("home")
    @Valid
    private CoordinateDTO home;

    /** 原字段 safety_buffer_m；约束 {"maximum": 100.0, "minimum": 0.0, "default": 12}。 */
    @JsonProperty("safety_buffer_m")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double safetyBufferM = 12D;

    /** 原字段 max_risk_score；约束 {"maximum": 100.0, "minimum": 0.0, "default": 75}。 */
    @JsonProperty("max_risk_score")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double maxRiskScore = 75D;

    /** 原字段 altitude_reference；约束 {"const": "AGL", "default": "AGL"}。 */
    @JsonProperty("altitude_reference")
    @NotNull
    @Pattern(regexp = "^AGL$")
    private String altitudeReference = "AGL";

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (reserveBatteryPercent == null || Double.isFinite(reserveBatteryPercent)) && (safetyBufferM == null || Double.isFinite(safetyBufferM)) && (maxRiskScore == null || Double.isFinite(maxRiskScore)); }

    @JsonIgnore
    @AssertTrue(message = "跨字段约束未满足，详见原接口文档")
    public boolean isConsistent() { return start == null || end == null || start.getLongitude() == null || end.getLongitude() == null || start.getLatitude() == null || end.getLatitude() == null || Math.abs(start.getLongitude()-end.getLongitude()) + Math.abs(start.getLatitude()-end.getLatitude()) >= 0.0001; }
}
