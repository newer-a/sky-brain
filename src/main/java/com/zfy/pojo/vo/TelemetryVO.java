package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 TelemetryVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class TelemetryVO {
    @JsonProperty("log_id")
    private Long logId;

    @JsonProperty("point_count")
    private Integer pointCount;

    @JsonProperty("cross_track_mean_m")
    private Double crossTrackMeanM;

    @JsonProperty("cross_track_max_m")
    private Double crossTrackMaxM;

    @JsonProperty("battery_drop_percent")
    private Double batteryDropPercent;

    @JsonProperty("source")
    private String source;

    @JsonProperty("last_point")
    private TelemetryPointDTO lastPoint;

    @JsonProperty("source_verified")
    private Boolean sourceVerified;

}
