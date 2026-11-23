package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python BenchmarkInput。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class BenchmarkDTO {
    /** 原字段 count；约束 {"maximum": 20.0, "minimum": 2.0, "default": 6}。 */
    @JsonProperty("count")
    @NotNull
    @DecimalMin(value = "2.0", inclusive = true)
    @DecimalMax(value = "20.0", inclusive = true)
    private Integer count = 6;
}
