package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 FrameVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FrameVO extends MapPointVO {
    @JsonProperty("t_s")
    private Double tS;

    @JsonProperty("battery_percent")
    private Double batteryPercent;

    @JsonProperty("risk_score")
    private Double riskScore;

}
