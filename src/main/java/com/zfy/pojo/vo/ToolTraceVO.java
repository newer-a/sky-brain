package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 ToolTraceVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolTraceVO {
    @JsonProperty("tool")
    private String tool;

    @JsonProperty("status")
    private String status;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("arguments")
    private Map<String,Object> arguments;

    @JsonProperty("result")
    private Map<String,Object> result;

}
