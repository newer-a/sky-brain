package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 规划请求环境与计算结果快照；对应表 planning_runs。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class PlanningRun {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** request_json；JSON NOT NULL。 */
    private String requestJson;
    /** result_json；JSON NOT NULL。 */
    private String resultJson;
    /** environment_json；JSON NOT NULL。 */
    private String environmentJson;
    /** algorithm_version；VARCHAR(80) NOT NULL。 */
    private String algorithmVersion;
    /** created_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime createdAt;
}
