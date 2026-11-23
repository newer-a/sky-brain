package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 AiStatusVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiStatusVO {
    @JsonProperty("configured")
    private Boolean configured;

    @JsonProperty("connected")
    private Boolean connected;

    @JsonProperty("model")
    private String model;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("models")
    private List<String> models;

}
