package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 DispatchResultVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class DispatchResultVO {
    @JsonProperty("algorithm")
    private String algorithm;

    @JsonProperty("assignments")
    private List<AssignmentVO> assignments;

    @JsonProperty("unscheduled")
    private List<Map<String,Object>> unscheduled;

    @JsonProperty("conflict_events")
    private List<Map<String,Object>> conflictEvents;

    @JsonProperty("unresolved_conflicts")
    private List<Map<String,Object>> unresolvedConflicts;

    @JsonProperty("minimum_separation_m")
    private Double minimumSeparationM;

    @JsonProperty("spatial_checked")
    private Boolean spatialChecked;

    @JsonProperty("makespan_s")
    private Double makespanS;

    @JsonProperty("fleet_after")
    private List<Map<String,Object>> fleetAfter;

    @JsonProperty("assumptions")
    private String assumptions;

}
