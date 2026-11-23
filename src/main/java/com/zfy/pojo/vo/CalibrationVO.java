package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 CalibrationVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class CalibrationVO {
    @JsonProperty("coefficient_wh_per_km")
    private Double coefficientWhPerKm;

    @JsonProperty("train_count")
    private Integer trainCount;

    @JsonProperty("test_count")
    private Integer testCount;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("baseline")
    private ErrorMetricsVO baseline;

    @JsonProperty("calibrated")
    private ErrorMetricsVO calibrated;

    @JsonProperty("test_rows")
    private List<Map<String,Object>> testRows;

    @JsonProperty("applied")
    private Boolean applied;

    @JsonProperty("input_contract")
    private String inputContract;

}
