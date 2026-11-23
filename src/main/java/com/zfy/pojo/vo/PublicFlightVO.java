package com.zfy.pojo.vo;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** 对应公开数据flights文件，不是数据库FlightLog。points保留功率等扩展采样字段。 */
@Data
public class PublicFlightVO {
    @JsonProperty("flight_id") private Long flightId;
    private String name;
    private String date;
    private String route;
    @JsonProperty("commanded_speed_mps") private Double commandedSpeedMps;
    @JsonProperty("payload_kg") private Double payloadKg;
    @JsonProperty("commanded_altitude_m") private Double commandedAltitudeM;
    @JsonProperty("duration_s") private Double durationS;
    @JsonProperty("distance_m") private Double distanceM;
    @JsonProperty("measured_energy_wh") private Double measuredEnergyWh;
    @JsonProperty("mean_power_w") private Double meanPowerW;
    @JsonProperty("raw_point_count") private Integer rawPointCount;
    @JsonProperty("valid_point_count") private Integer validPointCount;
    @JsonProperty("gaps_over_2s") private Integer gapsOver2s;
    @JsonProperty("source_id") private String sourceId;
    @JsonProperty("is_simulation") private Boolean isSimulation;
    private List<Map<String, Object>> points;
}
