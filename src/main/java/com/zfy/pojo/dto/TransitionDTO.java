package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python Transition。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class TransitionDTO {
    /** 原字段 status；约束 {"enum": ["paused", "running", "cancelled"]}。 */
    @JsonProperty("status")
    @NotNull
    @Pattern(regexp = "^(paused|running|cancelled)$")
    private String status;
}
