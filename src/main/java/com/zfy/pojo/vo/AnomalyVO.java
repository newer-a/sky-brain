package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 AnomalyVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class AnomalyVO {
    @JsonProperty("kind")
    private String kind;

    @JsonProperty("title")
    private String title;

    @JsonProperty("start_s")
    private Double startS;

    @JsonProperty("end_s")
    private Double endS;

    @JsonProperty("metric")
    private String metric;

    @JsonProperty("observed")
    private Double observed;

    @JsonProperty("threshold")
    private Double threshold;

    @JsonProperty("level")
    private String level;

    @JsonProperty("event_id")
    private String eventId;

}
