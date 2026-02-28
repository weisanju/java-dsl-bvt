-- Java + Karate 接口测试平台 MVP 数据库表结构草案
-- 数据库：PostgreSQL 15+
-- 说明：
-- 1) 本脚本为 MVP 版本，可直接作为建库初稿。
-- 2) 生产建议使用 Flyway / Liquibase 管理版本化迁移。

-- =========================
-- 1. 账号与权限
-- =========================

CREATE TABLE IF NOT EXISTS t_sys_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64) NOT NULL UNIQUE,
    display_name    VARCHAR(128) NOT NULL,
    email           VARCHAR(128),
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED')),
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      BIGINT,
    updated_by      BIGINT,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS t_sys_role (
    id              BIGSERIAL PRIMARY KEY,
    role_code       VARCHAR(64) NOT NULL UNIQUE,
    role_name       VARCHAR(128) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_sys_user_role (
    user_id         BIGINT NOT NULL REFERENCES t_sys_user(id) ON DELETE CASCADE,
    role_id         BIGINT NOT NULL REFERENCES t_sys_role(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- =========================
-- 2. 项目与成员
-- =========================

CREATE TABLE IF NOT EXISTS t_project (
    id              BIGSERIAL PRIMARY KEY,
    project_key     VARCHAR(64) NOT NULL UNIQUE,
    project_name    VARCHAR(128) NOT NULL,
    description     TEXT,
    owner_user_id   BIGINT NOT NULL REFERENCES t_sys_user(id),
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS t_project_member (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES t_sys_user(id) ON DELETE CASCADE,
    project_role    VARCHAR(16) NOT NULL
                    CHECK (project_role IN ('ADMIN', 'DEV', 'TESTER', 'VIEWER')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, user_id)
);

-- =========================
-- 3. 环境与变量
-- =========================

CREATE TABLE IF NOT EXISTS t_test_environment (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    env_code        VARCHAR(32) NOT NULL,
    env_name        VARCHAR(64) NOT NULL,
    base_url        VARCHAR(512) NOT NULL,
    description     TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    status          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, env_code)
);

CREATE TABLE IF NOT EXISTS t_env_variable (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    environment_id  BIGINT REFERENCES t_test_environment(id) ON DELETE CASCADE,
    var_key         VARCHAR(128) NOT NULL,
    var_value       TEXT,
    is_secret       BOOLEAN NOT NULL DEFAULT FALSE,
    secret_ref      VARCHAR(255),
    description     TEXT,
    is_enabled      BOOLEAN NOT NULL DEFAULT TRUE,
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
-- 4. 接口定义与测试资产
-- =========================

CREATE TABLE IF NOT EXISTS t_api_definition (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    module_name         VARCHAR(128),
    api_name            VARCHAR(128) NOT NULL,
    http_method         VARCHAR(8) NOT NULL
                        CHECK (http_method IN ('GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'HEAD', 'OPTIONS')),
    path                VARCHAR(512) NOT NULL,
    headers_template    JSONB,
    query_template      JSONB,
    body_template       JSONB,
    response_schema     JSONB,
    source_type         VARCHAR(16) NOT NULL DEFAULT 'MANUAL'
                        CHECK (source_type IN ('MANUAL', 'OPENAPI')),
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'DISABLED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, http_method, path)
);

CREATE TABLE IF NOT EXISTS t_test_case (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    api_id              BIGINT REFERENCES t_api_definition(id) ON DELETE SET NULL,
    case_code           VARCHAR(64) NOT NULL,
    case_name           VARCHAR(128) NOT NULL,
    description         TEXT,
    script_type         VARCHAR(16) NOT NULL DEFAULT 'KARATE'
                        CHECK (script_type IN ('KARATE')),
    script_content      TEXT NOT NULL,
    tags                JSONB,
    version_no          INTEGER NOT NULL DEFAULT 1,
    status              VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'ACTIVE', 'DISABLED')),
    created_by          BIGINT REFERENCES t_sys_user(id),
    updated_by          BIGINT REFERENCES t_sys_user(id),
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
    status              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'DISABLED')),
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
    created_by          BIGINT REFERENCES t_sys_user(id),
    updated_by          BIGINT REFERENCES t_sys_user(id),
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
-- 5. 计划、执行与 Runner
-- =========================

CREATE TABLE IF NOT EXISTS t_test_plan (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    plan_name           VARCHAR(128) NOT NULL,
    suite_id            BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id      BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    trigger_mode        VARCHAR(16) NOT NULL DEFAULT 'MANUAL'
                        CHECK (trigger_mode IN ('MANUAL', 'CRON', 'CI')),
    cron_expr           VARCHAR(128),
    ci_source           VARCHAR(32)
                        CHECK (ci_source IS NULL OR ci_source IN ('JENKINS', 'GITHUB_ACTIONS', 'GITLAB_CI')),
    branch_filter       VARCHAR(128),
    retry_count         SMALLINT NOT NULL DEFAULT 0 CHECK (retry_count >= 0 AND retry_count <= 5),
    max_concurrency     INTEGER NOT NULL DEFAULT 5 CHECK (max_concurrency >= 1 AND max_concurrency <= 100),
    status              VARCHAR(16) NOT NULL DEFAULT 'ENABLED'
                        CHECK (status IN ('ENABLED', 'DISABLED')),
    created_by          BIGINT REFERENCES t_sys_user(id),
    updated_by          BIGINT REFERENCES t_sys_user(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, plan_name)
);

CREATE TABLE IF NOT EXISTS t_runner_node (
    id                  BIGSERIAL PRIMARY KEY,
    node_code           VARCHAR(64) NOT NULL UNIQUE,
    host_ip             VARCHAR(64) NOT NULL,
    max_parallel        INTEGER NOT NULL DEFAULT 4 CHECK (max_parallel >= 1 AND max_parallel <= 256),
    capability_tags     JSONB,
    version             VARCHAR(32),
    status              VARCHAR(16) NOT NULL DEFAULT 'ONLINE'
                        CHECK (status IN ('ONLINE', 'OFFLINE', 'DISABLED')),
    heartbeat_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_test_run (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    plan_id             BIGINT REFERENCES t_test_plan(id) ON DELETE SET NULL,
    suite_id            BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id      BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    trigger_type        VARCHAR(16) NOT NULL
                        CHECK (trigger_type IN ('MANUAL', 'CRON', 'CI', 'API')),
    trigger_by          BIGINT REFERENCES t_sys_user(id),
    ci_build_id         VARCHAR(128),
    source_branch       VARCHAR(128),
    commit_sha          VARCHAR(64),
    queue_name          VARCHAR(64) NOT NULL DEFAULT 'default',
    status              VARCHAR(16) NOT NULL DEFAULT 'QUEUED'
                        CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'TIMEOUT')),
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

CREATE TABLE IF NOT EXISTS t_test_run_task (
    id                  BIGSERIAL PRIMARY KEY,
    run_id              BIGINT NOT NULL REFERENCES t_test_run(id) ON DELETE CASCADE,
    suite_case_id       BIGINT NOT NULL REFERENCES t_test_suite_case(id) ON DELETE RESTRICT,
    case_id             BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE RESTRICT,
    runner_id           BIGINT REFERENCES t_runner_node(id) ON DELETE SET NULL,
    attempt_no          SMALLINT NOT NULL DEFAULT 0 CHECK (attempt_no >= 0 AND attempt_no <= 5),
    status              VARCHAR(16) NOT NULL DEFAULT 'QUEUED'
                        CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'TIMEOUT')),
    queued_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    duration_ms         BIGINT,
    log_excerpt         TEXT,
    UNIQUE (run_id, suite_case_id, attempt_no)
);

CREATE TABLE IF NOT EXISTS t_test_case_result (
    id                  BIGSERIAL PRIMARY KEY,
    run_id              BIGINT NOT NULL REFERENCES t_test_run(id) ON DELETE CASCADE,
    task_id             BIGINT NOT NULL REFERENCES t_test_run_task(id) ON DELETE CASCADE,
    case_id             BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE RESTRICT,
    suite_case_id       BIGINT NOT NULL REFERENCES t_test_suite_case(id) ON DELETE RESTRICT,
    scenario_name       VARCHAR(255),
    status              VARCHAR(16) NOT NULL
                        CHECK (status IN ('PASSED', 'FAILED', 'SKIPPED', 'ERROR')),
    http_status         INTEGER,
    response_time_ms    INTEGER,
    assertion_total     INTEGER NOT NULL DEFAULT 0,
    assertion_passed    INTEGER NOT NULL DEFAULT 0,
    assertion_failed    INTEGER NOT NULL DEFAULT 0,
    request_snapshot    JSONB,
    response_snapshot   JSONB,
    error_message       TEXT,
    stack_trace         TEXT,
    started_at          TIMESTAMPTZ,
    finished_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_test_artifact (
    id                  BIGSERIAL PRIMARY KEY,
    run_id              BIGINT NOT NULL REFERENCES t_test_run(id) ON DELETE CASCADE,
    task_id             BIGINT REFERENCES t_test_run_task(id) ON DELETE CASCADE,
    result_id           BIGINT REFERENCES t_test_case_result(id) ON DELETE CASCADE,
    artifact_type       VARCHAR(32) NOT NULL
                        CHECK (artifact_type IN ('LOG', 'REPORT_HTML', 'REPORT_JSON', 'JUNIT_XML', 'REQUEST', 'RESPONSE', 'SCREENSHOT', 'OTHER')),
    storage_type        VARCHAR(16) NOT NULL DEFAULT 'MINIO'
                        CHECK (storage_type IN ('MINIO', 'S3', 'LOCAL')),
    bucket_name         VARCHAR(128),
    object_key          VARCHAR(512) NOT NULL,
    file_name           VARCHAR(255),
    content_type        VARCHAR(128),
    size_bytes          BIGINT,
    checksum_sha256     VARCHAR(64),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================
-- 6. 审计与通知
-- =========================

CREATE TABLE IF NOT EXISTS t_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT REFERENCES t_project(id) ON DELETE SET NULL,
    operator_user_id    BIGINT REFERENCES t_sys_user(id) ON DELETE SET NULL,
    action              VARCHAR(64) NOT NULL,
    resource_type       VARCHAR(64) NOT NULL,
    resource_id         VARCHAR(64),
    before_data         JSONB,
    after_data          JSONB,
    request_id          VARCHAR(128),
    ip_addr             VARCHAR(64),
    user_agent          VARCHAR(255),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_notification_channel (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    channel_name        VARCHAR(64) NOT NULL,
    channel_type        VARCHAR(16) NOT NULL
                        CHECK (channel_type IN ('FEISHU', 'WECOM', 'DINGTALK', 'EMAIL')),
    webhook_url         VARCHAR(1024),
    secret_token        VARCHAR(255),
    is_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, channel_name)
);

CREATE TABLE IF NOT EXISTS t_notification_rule (
    id                  BIGSERIAL PRIMARY KEY,
    project_id          BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    rule_name           VARCHAR(128) NOT NULL,
    event_type          VARCHAR(32) NOT NULL
                        CHECK (event_type IN ('RUN_SUCCESS', 'RUN_FAILED', 'RUN_FINISHED')),
    plan_id             BIGINT REFERENCES t_test_plan(id) ON DELETE CASCADE,
    suite_id            BIGINT REFERENCES t_test_suite(id) ON DELETE CASCADE,
    channel_id          BIGINT NOT NULL REFERENCES t_notification_channel(id) ON DELETE CASCADE,
    template            TEXT,
    is_enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================
-- 7. 核心索引（执行链路）
-- =========================

CREATE INDEX IF NOT EXISTS idx_t_project_owner
    ON t_project (owner_user_id);

CREATE INDEX IF NOT EXISTS idx_t_project_member_user
    ON t_project_member (user_id);

CREATE INDEX IF NOT EXISTS idx_t_test_case_project_status
    ON t_test_case (project_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_suite_project_status
    ON t_test_suite (project_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_plan_project_status
    ON t_test_plan (project_id, status);

CREATE INDEX IF NOT EXISTS idx_t_runner_node_status_heartbeat
    ON t_runner_node (status, heartbeat_at);

CREATE INDEX IF NOT EXISTS idx_t_test_run_project_created
    ON t_test_run (project_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_t_test_run_status_created
    ON t_test_run (status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_t_test_run_task_run_status
    ON t_test_run_task (run_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_run_task_runner_status
    ON t_test_run_task (runner_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_case_result_run_status
    ON t_test_case_result (run_id, status);

CREATE INDEX IF NOT EXISTS idx_t_test_artifact_run
    ON t_test_artifact (run_id, artifact_type);

CREATE INDEX IF NOT EXISTS idx_t_audit_log_project_created
    ON t_audit_log (project_id, created_at DESC);

-- =========================
-- 8. 备注
-- =========================
-- 1) updated_at 自动更新时间可在应用层统一处理，或补充 trigger。
-- 2) 密钥变量建议保存 secret_ref（引用 KMS/Vault），避免直接保存明文。
-- 3) 若 run/result 规模极大，建议按月分区 t_test_run、t_test_case_result。
