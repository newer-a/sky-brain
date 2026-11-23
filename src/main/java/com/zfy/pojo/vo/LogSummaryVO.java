package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 LogSummaryVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class LogSummaryVO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("is_simulation")
    private Boolean isSimulation;

    @JsonProperty("metrics")
    private LogMetricsVO metrics;

    @JsonProperty("imported_at")
    private String importedAt;

}
