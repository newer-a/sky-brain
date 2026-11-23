package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python FleetInput。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class FleetDTO {
    /** 原字段 plan_id；约束 {"exclusiveMinimum": 0.0}。 */
    @JsonProperty("plan_id")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Long planId;

    /** 原字段 route_key；约束 {}。 */
    @JsonProperty("route_key")
    private String routeKey;

    /** 原字段 message；约束 {"maxLength": 2000, "minLength": 2, "default": "比较时效与能耗，优先保证全部任务完成，给出最佳协同方案。"}。 */
    @JsonProperty("message")
    @NotNull
    @Size(min = 2, max = 2000)
    private String message = "比较时效与能耗，优先保证全部任务完成，给出最佳协同方案。";
}
