package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 DraftSummaryVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class DraftSummaryVO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("plan_id")
    private Long planId;

    @JsonProperty("created_at")
    private String createdAt;

}
