package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 LogMetricsVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class LogMetricsVO {
    @JsonProperty("point_count")
    private Integer pointCount;

    @JsonProperty("distance_m")
    private Double distanceM;

    @JsonProperty("battery_drop_percent")
    private Double batteryDropPercent;

    @JsonProperty("min_altitude_m")
    private Double minAltitudeM;

    @JsonProperty("max_altitude_m")
    private Double maxAltitudeM;

}
