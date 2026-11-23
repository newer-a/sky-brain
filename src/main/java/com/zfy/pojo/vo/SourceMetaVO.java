package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 SourceMetaVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SourceMetaVO {
    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("endpoint")
    private String endpoint;

    @JsonProperty("available")
    private Boolean available;

    @JsonProperty("is_historical_cache")
    private Boolean isHistoricalCache;

    @JsonProperty("is_simulation")
    private Boolean isSimulation;

    @JsonProperty("observed_at")
    private String observedAt;

    @JsonProperty("fetched_at")
    private String fetchedAt;

    @JsonProperty("record_count")
    private Integer recordCount;

    @JsonProperty("error")
    private String error;

}
