package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 FleetDraftVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class FleetDraftVO {
    @JsonProperty("analysis_id")
    private Long analysisId;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("selected_candidate_id")
    private String selectedCandidateId;

    @JsonProperty("selected")
    private FleetCandidateVO selected;

    @JsonProperty("baseline")
    private FleetMetricsVO baseline;

    @JsonProperty("candidates")
    private List<FleetCandidateSummaryVO> candidates;

    @JsonProperty("tool_trace")
    private List<ToolTraceVO> toolTrace;

    @JsonProperty("model")
    private String model;

    @JsonProperty("plan_id")
    private Long planId;

    @JsonProperty("route_key")
    private String routeKey;

    @JsonProperty("fleet_request")
    private DispatchDTO fleetRequest;

    @JsonProperty("applied")
    private Boolean applied;

}
