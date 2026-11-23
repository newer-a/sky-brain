package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 EnvironmentVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class EnvironmentVO {
    @JsonProperty("bbox")
    private List<Double> bbox;

    @JsonProperty("weather")
    private Map<String,Object> weather;

    @JsonProperty("buildings")
    private List<Map<String,Object>> buildings;

    @JsonProperty("communication_nodes")
    private List<Map<String,Object>> communicationNodes;

    @JsonProperty("elevation_samples")
    private List<Map<String,Object>> elevationSamples;

    @JsonProperty("sources")
    private List<SourceMetaVO> sources;

    @JsonProperty("data_mode_used")
    private String dataModeUsed;

    @JsonProperty("simulation_notice")
    private String simulationNotice;

    @JsonProperty("simulation_restrictions")
    private List<Map<String,Object>> simulationRestrictions;

}
