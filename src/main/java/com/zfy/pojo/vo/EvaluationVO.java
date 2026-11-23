package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 EvaluationVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class EvaluationVO {
    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("baseline")
    private String baseline;

    @JsonProperty("comparisons")
    private List<ComparedRouteVO> comparisons;

    @JsonProperty("sources")
    private List<SourceMetaVO> sources;

    @JsonProperty("data_mode_used")
    private String dataModeUsed;

    @JsonProperty("simulation_notice")
    private String simulationNotice;

    @JsonProperty("computed_at")
    private String computedAt;

}
