package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 巡检任务；对应表 missions。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class Mission {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** name；VARCHAR(180) NOT NULL。 */
    private String name;
    /** task_type；VARCHAR(60) NOT NULL。 */
    private String taskType;
    /** status；VARCHAR(40) NOT NULL DEFAULT 'planned'。 */
    private String status;
    /** execution_mode；VARCHAR(40) NOT NULL DEFAULT 'SIMULATION'。 */
    private String executionMode;
    /** plan_id；BIGINT NOT NULL。 */
    private Long planId;
    /** route_key；VARCHAR(50) NOT NULL DEFAULT 'balanced'。 */
    private String routeKey;
    /** drone_json；JSON NOT NULL。 */
    private String droneJson;
    /** created_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime createdAt;
    /** updated_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime updatedAt;
}
