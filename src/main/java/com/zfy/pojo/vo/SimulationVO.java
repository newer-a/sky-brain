package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 SimulationVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class SimulationVO {
    @JsonProperty("mission_id")
    private Long missionId;

    @JsonProperty("execution_mode")
    private String executionMode;

    @JsonProperty("disclaimer")
    private String disclaimer;

    @JsonProperty("route")
    private RouteVO route;

    @JsonProperty("frames")
    private List<FrameVO> frames;

}
