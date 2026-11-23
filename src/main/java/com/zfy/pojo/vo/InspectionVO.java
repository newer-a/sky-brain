package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 InspectionVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class InspectionVO {
    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("category")
    private String category;

    @JsonProperty("title")
    private String title;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("observations")
    private List<String> observations;

    @JsonProperty("suggested_action")
    private String suggestedAction;

    @JsonProperty("image_sha256")
    private String imageSha256;

    @JsonProperty("status")
    private String status;

    @JsonProperty("model")
    private String model;

    @JsonProperty("location_source")
    private String locationSource;

}
