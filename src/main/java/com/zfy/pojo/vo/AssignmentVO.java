package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 AssignmentVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class AssignmentVO {
    @JsonProperty("job_id")
    private String jobId;

    @JsonProperty("job_name")
    private String jobName;

    @JsonProperty("drone_id")
    private String droneId;

    @JsonProperty("drone_name")
    private String droneName;

    @JsonProperty("start_s")
    private Double startS;

    @JsonProperty("finish_s")
    private Double finishS;

    @JsonProperty("energy_used_percent")
    private Double energyUsedPercent;

    @JsonProperty("energy_wh")
    private Double energyWh;

    @JsonProperty("remaining_battery_percent")
    private Double remainingBatteryPercent;

    @JsonProperty("delay_s")
    private Double delayS;

    @JsonProperty("coordinates")
    private List<CoordinateDTO> coordinates;

    @JsonProperty("spatial_checked")
    private Boolean spatialChecked;

}
