package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 DiagnosisVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class DiagnosisVO {
    @JsonProperty("analysis_id")
    private Long analysisId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("findings")
    private List<FindingVO> findings;

    @JsonProperty("metrics")
    private Map<String,Object> metrics;

    @JsonProperty("events")
    private List<AnomalyVO> events;

    @JsonProperty("reference")
    private Map<String,Object> reference;

    @JsonProperty("series")
    private List<Map<String,Object>> series;

    @JsonProperty("record_name")
    private String recordName;

    @JsonProperty("record_date")
    private String recordDate;

    @JsonProperty("tool_trace")
    private List<ToolTraceVO> toolTrace;

    @JsonProperty("model")
    private String model;

    @JsonProperty("source")
    private String source;

    @JsonProperty("record_id")
    private Long recordId;

}
