package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 MissionCreatedVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class MissionCreatedVO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("execution_mode")
    private String executionMode;

    @JsonProperty("created_at")
    private String createdAt;

}
