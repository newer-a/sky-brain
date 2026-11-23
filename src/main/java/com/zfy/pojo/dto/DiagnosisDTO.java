package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python DiagnosisInput。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class DiagnosisDTO {
    /** 原字段 source；约束 {"enum": ["public", "archive"], "default": "public"}。 */
    @JsonProperty("source")
    @NotNull
    @Pattern(regexp = "^(public|archive)$")
    private String source = "public";

    /** 原字段 record_id；约束 {"exclusiveMinimum": 0.0}。 */
    @JsonProperty("record_id")
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Long recordId;

    /** 原字段 message；约束 {"maxLength": 2000, "minLength": 2, "default": "分析本次飞行的功率、能耗和日志异常，按区间给出可能原因和处置建议。"}。 */
    @JsonProperty("message")
    @NotNull
    @Size(min = 2, max = 2000)
    private String message = "分析本次飞行的功率、能耗和日志异常，按区间给出可能原因和处置建议。";
}
