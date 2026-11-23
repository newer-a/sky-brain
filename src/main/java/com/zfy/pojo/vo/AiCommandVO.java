package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 AiCommandVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiCommandVO {
    @JsonProperty("available")
    private Boolean available;

    @JsonProperty("status")
    private String status;

    @JsonProperty("model")
    private String model;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("tool_trace")
    private List<ToolTraceVO> toolTrace;

    @JsonProperty("usage")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Map<String,Object> usage;

}
