package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 ReplanVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReplanVO extends PlanningResultVO {
    @JsonProperty("old_plan_id")
    private Long oldPlanId;

    @JsonProperty("new_plan_id")
    private Long newPlanId;

    @JsonProperty("from_live_position")
    private Boolean fromLivePosition;

    @JsonProperty("event")
    private Map<String,Object> event;

    @JsonProperty("sources")
    private List<SourceMetaVO> sources;

    @JsonProperty("buildings")
    private List<Map<String,Object>> buildings;

    @JsonProperty("data_mode_used")
    private String dataModeUsed;

    @JsonProperty("simulation_notice")
    private String simulationNotice;

}
