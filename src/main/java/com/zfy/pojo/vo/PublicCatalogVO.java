package com.zfy.pojo.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** 公开数据目录；source/model是带版本的外部数据元信息，不是业务实体。 */
@Data
public class PublicCatalogVO {
    private List<Map<String, Object>> flights;
    private List<Map<String, Object>> rejected;
    @JsonProperty("total_points")
    private Long totalPoints;
    @JsonProperty("total_energy_wh")
    private Double totalEnergyWh;
    private Map<String, Object> source;
    private Map<String, Object> model;
}
