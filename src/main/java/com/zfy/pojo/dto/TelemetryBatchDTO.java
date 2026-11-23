package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python TelemetryBatch。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class TelemetryBatchDTO {
    /** 原字段 source；约束 {"enum": ["SITL", "FLIGHT_LOG", "MAVLINK"]}。 */
    @JsonProperty("source")
    @NotNull
    @Pattern(regexp = "^(SITL|FLIGHT_LOG|MAVLINK)$")
    private String source;

    /** 原字段 points；约束 {"maxItems": 2000, "minItems": 2}。 */
    @JsonProperty("points")
    @NotNull
    @Valid
    @Size(min = 2, max = 2000)
    private List<@NotNull TelemetryPointDTO> points;

    @JsonIgnore
    @AssertTrue(message = "跨字段约束未满足，详见原接口文档")
    public boolean isConsistent() { if (points == null) return true; for (int i=1;i<points.size();i++) { var a=points.get(i-1); var b=points.get(i); if(a==null || b==null || a.getTS()==null || b.getTS()==null) return false; if(b.getTS()<=a.getTS()) return false; } return true; }
}
