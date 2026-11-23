package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python ReplanRequest。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class ReplanDTO {
    /** 原字段 plan_id；约束 {}。 */
    @JsonProperty("plan_id")
    @NotNull
    private Long planId;

    /** 原字段 event_name；约束 {"default": "临时风险事件"}。 */
    @JsonProperty("event_name")
    @NotNull
    private String eventName = "临时风险事件";

    /** 原字段 severity；约束 {"maximum": 100.0, "minimum": 0.0, "default": 100}。 */
    @JsonProperty("severity")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double severity = 100D;

    /** 原字段 geojson；约束 {"additionalProperties": true}。 */
    @JsonProperty("geojson")
    @NotNull
    private Map<String, Object> geojson;

    /** 原字段 current_position；约束 {}。 */
    @JsonProperty("current_position")
    @Valid
    private CoordinateDTO currentPosition;

    /** 原字段 battery_percent；约束 {}。 */
    @JsonProperty("battery_percent")
    @DecimalMin("0")
    @DecimalMax("100")
    private Double batteryPercent;

    /** 原字段 mission_id；约束 {}。 */
    @JsonProperty("mission_id")
    private Long missionId;

    /** 原字段 remaining_waypoints；约束 {}。 */
    @JsonProperty("remaining_waypoints")
    @Valid
    private List<@NotNull CoordinateDTO> remainingWaypoints;

    @JsonIgnore
    @AssertTrue(message = "数值必须为有限数")
    public boolean isFiniteNumbers() { return (severity == null || Double.isFinite(severity)) && (batteryPercent == null || Double.isFinite(batteryPercent)); }

    @JsonIgnore
    @AssertTrue(message = "跨字段约束未满足，详见原接口文档")
    public boolean isConsistent() { return (currentPosition == null) == (batteryPercent == null); }
}
