package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python MissionCreate。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class MissionCreateDTO {
    /** 原字段 name；约束 {"maxLength": 180, "minLength": 2}。 */
    @JsonProperty("name")
    @NotNull
    @Size(min = 2, max = 180)
    private String name;

    /** 原字段 task_type；约束 {}。 */
    @JsonProperty("task_type")
    @NotNull
    private String taskType;

    /** 原字段 plan_id；约束 {}。 */
    @JsonProperty("plan_id")
    @NotNull
    private Long planId;

    /** 原字段 route_key；约束 {"default": "balanced"}。 */
    @JsonProperty("route_key")
    @NotNull
    private String routeKey = "balanced";
}
