
CREATE DATABASE IF NOT EXISTS sky_brain CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE sky_brain;
SET time_zone = '+00:00';

CREATE TABLE data_source_snapshots (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_key VARCHAR(80) NOT NULL COMMENT 'source_key',
    source_name VARCHAR(160) NOT NULL COMMENT 'source_name',
    endpoint VARCHAR(500) NOT NULL COMMENT 'endpoint',
    available BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'available',
    is_cache BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'is_cache',
    observed_at DATETIME(6) NULL COMMENT 'observed_at',
    fetched_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'fetched_at',
    error TEXT NULL COMMENT 'error',
    record_count INT NOT NULL DEFAULT 0 COMMENT 'record_count',
    payload_json JSON NOT NULL COMMENT 'payload_json',
    KEY idx_source_fetched (source_key, fetched_at),
    CHECK (record_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据源抓取快照';

CREATE TABLE restriction_zones (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL COMMENT 'name',
    category VARCHAR(60) NOT NULL DEFAULT 'no_fly' COMMENT 'category',
    severity DOUBLE NOT NULL DEFAULT 100 COMMENT 'severity',
    min_altitude_m DOUBLE NOT NULL DEFAULT 0 COMMENT 'min_altitude_m',
    max_altitude_m DOUBLE NOT NULL DEFAULT 500 COMMENT 'max_altitude_m',
    source_name VARCHAR(180) NOT NULL COMMENT 'source_name',
    source_url VARCHAR(500) NULL COMMENT 'source_url',
    effective_from DATETIME(6) NULL COMMENT 'effective_from',
    effective_to DATETIME(6) NULL COMMENT 'effective_to',
    geojson JSON NOT NULL COMMENT 'geojson',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created_at',
    KEY idx_restriction_created (created_at),
    KEY idx_restriction_effective (effective_from, effective_to),
    CHECK (severity BETWEEN 0 AND 100),
    CHECK (min_altitude_m >= 0 AND max_altitude_m >= min_altitude_m),
    CHECK (effective_to IS NULL OR effective_from IS NULL OR effective_to >= effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='限制区域';

CREATE TABLE planning_runs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_json JSON NOT NULL COMMENT 'request_json',
    result_json JSON NOT NULL COMMENT 'result_json',
    environment_json JSON NOT NULL COMMENT 'environment_json',
    algorithm_version VARCHAR(80) NOT NULL COMMENT 'algorithm_version',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created_at',
    KEY idx_plan_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='规划请求环境与计算结果快照';

CREATE TABLE missions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL COMMENT 'name',
    task_type VARCHAR(60) NOT NULL COMMENT 'task_type',
    status VARCHAR(40) NOT NULL DEFAULT 'planned' COMMENT 'status',
    execution_mode VARCHAR(40) NOT NULL DEFAULT 'SIMULATION' COMMENT 'execution_mode',
    plan_id BIGINT NOT NULL COMMENT 'plan_id',
    route_key VARCHAR(50) NOT NULL DEFAULT 'balanced' COMMENT 'route_key',
    drone_json JSON NOT NULL COMMENT 'drone_json',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created_at',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'updated_at',
    KEY idx_mission_status_created (status, created_at),
    KEY idx_mission_plan (plan_id),
    CONSTRAINT fk_mission_plan FOREIGN KEY (plan_id) REFERENCES planning_runs(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡检任务';

CREATE TABLE flight_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(180) NOT NULL COMMENT 'name',
    source_name VARCHAR(180) NOT NULL COMMENT 'source_name',
    is_simulation BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'is_simulation',
    points_json JSON NOT NULL COMMENT 'points_json',
    metrics_json JSON NOT NULL COMMENT 'metrics_json',
    imported_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'imported_at',
    KEY idx_log_imported (imported_at),
    KEY idx_log_source (source_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='飞行日志与统计指标';

CREATE TABLE planning_drafts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    mode VARCHAR(30) NOT NULL COMMENT 'mode',
    status VARCHAR(30) NOT NULL DEFAULT 'draft' COMMENT 'status',
    prompt TEXT NOT NULL COMMENT 'prompt',
    payload_json JSON NOT NULL COMMENT 'payload_json',
    applied_plan_id BIGINT NULL COMMENT 'applied_plan_id',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created_at',
    KEY idx_draft_status_created (status, created_at),
    KEY idx_draft_plan (applied_plan_id),
    CONSTRAINT fk_draft_plan FOREIGN KEY (applied_plan_id) REFERENCES planning_runs(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='待采用的规划草案';

CREATE TABLE operation_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    kind VARCHAR(60) NOT NULL COMMENT 'kind',
    mission_id BIGINT NULL COMMENT 'mission_id',
    detail_json JSON NOT NULL COMMENT 'detail_json',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'created_at',
    KEY idx_event_kind_created (kind, created_at),
    KEY idx_event_mission_created (mission_id, created_at),
    CONSTRAINT fk_event_mission FOREIGN KEY (mission_id) REFERENCES missions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务与AI操作事件';
