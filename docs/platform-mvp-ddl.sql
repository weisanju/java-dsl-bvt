-- 极简版 DDL（单人实现 / 无中间件 / QLExpress 自定义引擎）
-- 技术栈：Spring Boot + PostgreSQL + QLExpress
-- 说明：
-- 1) 仅保留 MVP 必需表；
-- 2) 不包含用户权限、消息队列、runner 节点、通知等扩展模型；
-- 3) 用例定义支持 YAML/JSON 原文入库，并归一化为 JSONB 执行。

-- =========================
-- 1. 项目与环境
-- =========================

CREATE TABLE IF NOT EXISTS t_project (
    id              BIGSERIAL PRIMARY KEY,
    project_key     VARCHAR(64) NOT NULL UNIQUE,
    project_name    VARCHAR(128) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_test_environment (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    env_code        VARCHAR(32) NOT NULL,
    env_name        VARCHAR(64) NOT NULL,
    base_url        VARCHAR(512) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, env_code)
);

CREATE TABLE IF NOT EXISTS t_env_variable (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    environment_id  BIGINT REFERENCES t_test_environment(id) ON DELETE CASCADE,
    var_key         VARCHAR(128) NOT NULL,
    var_value       TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_env_variable_global
    ON t_env_variable (project_id, var_key)
    WHERE environment_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_env_variable_env
    ON t_env_variable (project_id, environment_id, var_key)
    WHERE environment_id IS NOT NULL;

-- =========================
-- 2. 测试资产（Case / DataSet / Suite）
-- =========================

CREATE TABLE IF NOT EXISTS t_test_case (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    case_code           VARCHAR(64) NOT NULL,
    case_name           VARCHAR(128) NOT NULL,
    engine_type         VARCHAR(16) NOT NULL DEFAULT 'QLEXPRESS'
                        CHECK (engine_type IN ('QLEXPRESS')),
    case_schema_version INTEGER NOT NULL DEFAULT 1,
    config_format       VARCHAR(8) NOT NULL CHECK (config_format IN ('JSON', 'YAML')),
    config_content      TEXT NOT NULL,
    normalized_definition JSONB NOT NULL,
    tags                JSONB,
    version_no          INTEGER NOT NULL DEFAULT 1,
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, case_code)
);

CREATE TABLE IF NOT EXISTS t_test_case_data_set (
    id                  BIGSERIAL PRIMARY KEY,
    case_id             BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE CASCADE,
    data_set_name       VARCHAR(128) NOT NULL,
    data_format         VARCHAR(8) NOT NULL CHECK (data_format IN ('JSON', 'YAML')),
    data_content        TEXT NOT NULL,
    is_default          BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (case_id, data_set_name)
);

CREATE TABLE IF NOT EXISTS t_test_suite (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    suite_code          VARCHAR(64) NOT NULL,
    suite_name          VARCHAR(128) NOT NULL,
    description         TEXT,
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, suite_code)
);

CREATE TABLE IF NOT EXISTS t_test_suite_case (
    id                  BIGSERIAL PRIMARY KEY,
    suite_id            BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE CASCADE,
    case_id             BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE CASCADE,
    data_set_id         BIGINT REFERENCES t_test_case_data_set(id) ON DELETE SET NULL,
    exec_order          INTEGER NOT NULL DEFAULT 0,
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_test_suite_case_without_data_set
    ON t_test_suite_case (suite_id, case_id)
    WHERE data_set_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_test_suite_case_with_data_set
    ON t_test_suite_case (suite_id, case_id, data_set_id)
    WHERE data_set_id IS NOT NULL;

-- =========================
-- 3. 执行与报告
-- =========================

CREATE TABLE IF NOT EXISTS t_test_run (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    suite_id            BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id      BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    trigger_type        VARCHAR(16) NOT NULL
                        CHECK (trigger_type IN ('MANUAL', 'CRON', 'API')),
    status              VARCHAR(16) NOT NULL DEFAULT 'QUEUED'
                        CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED')),
    total_cases         INTEGER NOT NULL DEFAULT 0,
    passed_cases        INTEGER NOT NULL DEFAULT 0,
    failed_cases        INTEGER NOT NULL DEFAULT 0,
    skipped_cases       INTEGER NOT NULL DEFAULT 0,
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    duration_ms         BIGINT,
    error_summary       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_test_case_result (
    id                  BIGSERIAL PRIMARY KEY,
    run_id              BIGINT NOT NULL REFERENCES t_test_run(id) ON DELETE CASCADE,
    case_id             BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE RESTRICT,
    suite_case_id       BIGINT REFERENCES t_test_suite_case(id) ON DELETE SET NULL,
    step_no             INTEGER,
    step_name           VARCHAR(128),
    status              VARCHAR(16) NOT NULL
                        CHECK (status IN ('PASSED', 'FAILED', 'SKIPPED', 'ERROR')),
    response_time_ms    INTEGER,
    assertion_failed    INTEGER NOT NULL DEFAULT 0,
    failed_assert_expr  TEXT,
    request_snapshot    JSONB,
    response_snapshot   JSONB,
    extracted_vars      JSONB,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================
-- 4. 定时计划（P1 轻量）
-- =========================

CREATE TABLE IF NOT EXISTS t_test_plan (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    plan_name           VARCHAR(128) NOT NULL,
    suite_id            BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id      BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    cron_expr           VARCHAR(128) NOT NULL,
    status              VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
                        CHECK (status IN ('ENABLED', 'DISABLED')),
    last_run_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, plan_name)
);

-- =========================
-- 5. 关键索引
-- =========================

CREATE INDEX IF NOT EXISTS idx_t_test_case_project_status
    ON t_test_case (project_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_suite_project_status
    ON t_test_suite (project_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_run_project_created
    ON t_test_run (project_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_t_test_run_status_created
    ON t_test_run (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_t_test_case_result_run_status
    ON t_test_case_result (run_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_plan_status
    ON t_test_plan (status, updated_at DESC);

-- 备注：
-- 1) updated_at 可由应用层统一维护；
-- 2) config_content 保存原始 YAML/JSON，normalized_definition 用于执行；
-- 3) 后续若规模增长，可增加 MQ/缓存/分布式执行能力。
