package com.zfy.pojo.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;

/** 原Python InspectionInput。Controller请求参数需加 @Valid 才启用Bean Validation；业务规则由Service处理。 */
@Data
public class InspectionDTO {
    /** 原字段 image_data_url；约束 {"maxLength": 6000000}。 */
    @JsonProperty("image_data_url")
    @NotNull
    @Size(max = 6000000)
    private String imageDataUrl;

    /** 原字段 task；约束 {"maxLength": 2000, "default": "识别图中可能影响园区巡检的烟火、积水、道路阻塞或设施损坏。"}。 */
    @JsonProperty("task")
    @NotNull
    @Size(max = 2000)
    private String task = "识别图中可能影响园区巡检的烟火、积水、道路阻塞或设施损坏。";
}
