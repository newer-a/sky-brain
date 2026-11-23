package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 DashboardVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class DashboardVO {
    @JsonProperty("missions")
    private Long missions;

    @JsonProperty("active_missions")
    private Long activeMissions;

    @JsonProperty("planning_runs")
    private Long planningRuns;

    @JsonProperty("restriction_zones")
    private Long restrictionZones;

    @JsonProperty("latest_plan_id")
    private Long latestPlanId;

    @JsonProperty("data_sources")
    private List<SourceMetaVO> dataSources;

    @JsonProperty("ai")
    private AiSummaryVO ai;

    @JsonProperty("computed_at")
    private String computedAt;

}
