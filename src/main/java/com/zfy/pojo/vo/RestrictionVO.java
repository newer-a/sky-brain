package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 RestrictionVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class RestrictionVO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("category")
    private String category;

    @JsonProperty("severity")
    private Double severity;

    @JsonProperty("min_altitude_m")
    private Double minAltitudeM;

    @JsonProperty("max_altitude_m")
    private Double maxAltitudeM;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("source_url")
    private String sourceUrl;

    @JsonProperty("geojson")
    private Map<String,Object> geojson;

    @JsonProperty("created_at")
    private String createdAt;

}
