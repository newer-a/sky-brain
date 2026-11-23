package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 数据源抓取快照；对应表 data_source_snapshots。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class DataSourceSnapshot {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** source_key；VARCHAR(80) NOT NULL。 */
    private String sourceKey;
    /** source_name；VARCHAR(160) NOT NULL。 */
    private String sourceName;
    /** endpoint；VARCHAR(500) NOT NULL。 */
    private String endpoint;
    /** available；BOOLEAN NOT NULL DEFAULT FALSE。 */
    private Boolean available;
    /** is_cache；BOOLEAN NOT NULL DEFAULT FALSE。 */
    private Boolean isCache;
    /** observed_at；DATETIME(6) NULL。 */
    private LocalDateTime observedAt;
    /** fetched_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime fetchedAt;
    /** error；TEXT NULL。 */
    private String error;
    /** record_count；INT NOT NULL DEFAULT 0。 */
    private Integer recordCount;
    /** payload_json；JSON NOT NULL。 */
    private String payloadJson;
}
