package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 任务与AI操作事件；对应表 operation_events。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class OperationEvent {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** kind；VARCHAR(60) NOT NULL。 */
    private String kind;
    /** mission_id；BIGINT NULL。 */
    private Long missionId;
    /** detail_json；JSON NOT NULL。 */
    private String detailJson;
    /** created_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime createdAt;
}
