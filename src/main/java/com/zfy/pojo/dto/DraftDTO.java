package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DraftInput。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DraftDTO {
    /** 原字段 message；约束 {"maxLength": 4000, "minLength": 2}。 */
    @JsonProperty("message")
    @NotNull
    @Size(min = 2, max = 4000)
    private String message;

    /** 原字段 request；约束 {}。 */
    @JsonProperty("request")
    @NotNull
    @Valid
    private PlanDTO request;

    /** 原字段 mode；约束 {"enum": ["llm", "offline"], "default": "llm"}。 */
    @JsonProperty("mode")
    @NotNull
    @Pattern(regexp = "^(llm|offline)$")
    private String mode = "llm";
}
