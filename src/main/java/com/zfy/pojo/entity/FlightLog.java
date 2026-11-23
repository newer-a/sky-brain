package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 飞行日志与统计指标；对应表 flight_logs。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class FlightLog {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** name；VARCHAR(180) NOT NULL。 */
    private String name;
    /** source_name；VARCHAR(180) NOT NULL。 */
    private String sourceName;
    /** is_simulation；BOOLEAN NOT NULL DEFAULT FALSE。 */
    private Boolean isSimulation;
    /** points_json；JSON NOT NULL。 */
    private String pointsJson;
    /** metrics_json；JSON NOT NULL。 */
    private String metricsJson;
    /** imported_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime importedAt;
}
