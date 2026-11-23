package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python ReplayImport。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class ReplayImportDTO {
    /** 原字段 name；约束 {}。 */
    @JsonProperty("name")
    @NotNull
    private String name;

    /** 原字段 source_name；约束 {}。 */
    @JsonProperty("source_name")
    @NotNull
    private String sourceName;

    /** 原字段 is_simulation；约束 {"default": false}。 */
    @JsonProperty("is_simulation")
    @NotNull
    private Boolean isSimulation = false;

    /** 原字段 points；约束 {"minItems": 2}。 */
    @JsonProperty("points")
    @NotNull
    @Valid
    @Size(min = 2)
    private List<@NotNull Map<String, Object>> points;
    @JsonIgnore
    @AssertTrue(message = "日志点须有合法经纬度，数值须有限，时间不得倒序")
    public boolean isPointsValid() {
        if (points == null) return true;
        Double previous = null;
        for (Map<String, Object> point : points) {
            if (point == null || point.get("longitude") == null || point.get("latitude") == null) return false;
            String[] names = {"longitude", "latitude", "altitude_m", "battery_percent", "t_s"};
            double[] low = {-180, -90, -500, 0, 0};
            double[] high = {180, 90, 10000, 100, Double.MAX_VALUE};
            for (int i = 0; i < names.length; i++) {
                Object raw = point.get(names[i]);
                if (raw == null) continue;
                double value;
                try { value = Double.parseDouble(raw.toString()); }
                catch (NumberFormatException e) { return false; }
                if (!Double.isFinite(value) || value < low[i] || value > high[i]) return false;
                if (i == 4) {
                    if (previous != null && value < previous) return false;
                    previous = value;
                }
            }
        }
        return true;
    }
}
