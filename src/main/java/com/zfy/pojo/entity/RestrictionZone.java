package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 限制区域；对应表 restriction_zones。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class RestrictionZone {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** name；VARCHAR(180) NOT NULL。 */
    private String name;
    /** category；VARCHAR(60) NOT NULL DEFAULT 'no_fly'。 */
    private String category;
    /** severity；DOUBLE NOT NULL DEFAULT 100。 */
    private Double severity;
    /** min_altitude_m；DOUBLE NOT NULL DEFAULT 0。 */
    private Double minAltitudeM;
    /** max_altitude_m；DOUBLE NOT NULL DEFAULT 500。 */
    private Double maxAltitudeM;
    /** source_name；VARCHAR(180) NOT NULL。 */
    private String sourceName;
    /** source_url；VARCHAR(500) NULL。 */
    private String sourceUrl;
    /** effective_from；DATETIME(6) NULL。 */
    private LocalDateTime effectiveFrom;
    /** effective_to；DATETIME(6) NULL。 */
    private LocalDateTime effectiveTo;
    /** geojson；JSON NOT NULL。 */
    private String geojson;
    /** created_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime createdAt;
}
