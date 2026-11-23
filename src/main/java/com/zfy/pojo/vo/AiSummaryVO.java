package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 AiSummaryVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class AiSummaryVO {
    @JsonProperty("available")
    private Boolean available;

    @JsonProperty("configured")
    private Boolean configured;

    @JsonProperty("connection_verified")
    private Boolean connectionVerified;

    @JsonProperty("model")
    private String model;

}
