package com.zfy.pojo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 待采用的规划草案；对应表 planning_drafts。JSON列以String存取，禁止直接作为接口VO返回。 */
@Data
public class PlanningDraft {
    /** 数据库自增主键，插入时不赋值。 */
    private Long id;
    /** mode；VARCHAR(30) NOT NULL。 */
    private String mode;
    /** status；VARCHAR(30) NOT NULL DEFAULT 'draft'。 */
    private String status;
    /** prompt；TEXT NOT NULL。 */
    private String prompt;
    /** payload_json；JSON NOT NULL。 */
    private String payloadJson;
    /** applied_plan_id；BIGINT NULL。 */
    private Long appliedPlanId;
    /** created_at；DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)。 */
    private LocalDateTime createdAt;
}
