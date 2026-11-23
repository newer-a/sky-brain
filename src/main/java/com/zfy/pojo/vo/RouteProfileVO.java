package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 RouteProfileVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class RouteProfileVO {
    @JsonProperty("distance_m")
    private Double distanceM;

    @JsonProperty("risk")
    private Double risk;

    @JsonProperty("altitude_m")
    private Double altitudeM;

    @JsonProperty("elevation_m")
    private Double elevationM;

    @JsonProperty("battery_percent")
    private Double batteryPercent;

}
