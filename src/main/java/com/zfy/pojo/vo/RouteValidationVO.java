package com.zfy.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.*;
import com.zfy.pojo.dto.*;

/** 原接口响应 RouteValidationVO。日期字符串保持原ISO格式；不得直接返回数据库JSON字符串。 */
@Data
public class RouteValidationVO {
    @JsonProperty("passed")
    private Boolean passed;

    @JsonProperty("checks")
    private List<String> checks;

    @JsonProperty("model_scope")
    private String modelScope;

    @JsonProperty("altitude_reference")
    private String altitudeReference;

}
