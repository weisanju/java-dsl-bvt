CREATE TABLE IF NOT EXISTS t_project (
    id BIGSERIAL PRIMARY KEY,
    project_key VARCHAR(64) NOT NULL UNIQUE,
    project_name VARCHAR(128) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS t_test_environment (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    env_code VARCHAR(32) NOT NULL,
    env_name VARCHAR(64) NOT NULL,
    base_url VARCHAR(512) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, env_code)
);

CREATE TABLE IF NOT EXISTS t_env_variable (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    environment_id BIGINT REFERENCES t_test_environment(id) ON DELETE CASCADE,
    var_key VARCHAR(128) NOT NULL,
    var_value TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_env_variable_global
    ON t_env_variable (project_id, var_key)
    WHERE environment_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_env_variable_env
    ON t_env_variable (project_id, environment_id, var_key)
    WHERE environment_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS t_test_case (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    case_code VARCHAR(64) NOT NULL,
    case_name VARCHAR(128) NOT NULL,
    engine_type VARCHAR(16) NOT NULL DEFAULT 'QLEXPRESS',
    case_schema_version INTEGER NOT NULL DEFAULT 1,
    config_format VARCHAR(8) NOT NULL,
    config_content TEXT NOT NULL,
    normalized_definition JSONB NOT NULL,
    tags JSONB,
    version_no INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, case_code),
    CONSTRAINT ck_case_engine_type CHECK (engine_type IN ('QLEXPRESS')),
    CONSTRAINT ck_case_config_format CHECK (config_format IN ('JSON', 'YAML')),
    CONSTRAINT ck_case_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE IF NOT EXISTS t_test_case_data_set (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE CASCADE,
    data_set_name VARCHAR(128) NOT NULL,
    data_format VARCHAR(8) NOT NULL,
    data_content TEXT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (case_id, data_set_name),
    CONSTRAINT ck_data_set_format CHECK (data_format IN ('JSON', 'YAML'))
);

CREATE TABLE IF NOT EXISTS t_test_suite (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    suite_code VARCHAR(64) NOT NULL,
    suite_name VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, suite_code),
    CONSTRAINT ck_suite_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE TABLE IF NOT EXISTS t_test_suite_case (
    id BIGSERIAL PRIMARY KEY,
    suite_id BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE CASCADE,
    case_id BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE CASCADE,
    data_set_id BIGINT REFERENCES t_test_case_data_set(id) ON DELETE SET NULL,
    exec_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_test_suite_case_without_data_set
    ON t_test_suite_case (suite_id, case_id)
    WHERE data_set_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_t_test_suite_case_with_data_set
    ON t_test_suite_case (suite_id, case_id, data_set_id)
    WHERE data_set_id IS NOT NULL;

CREATE TABLE IF NOT EXISTS t_test_run (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    suite_id BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    trigger_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'QUEUED',
    total_cases INTEGER NOT NULL DEFAULT 0,
    passed_cases INTEGER NOT NULL DEFAULT 0,
    failed_cases INTEGER NOT NULL DEFAULT 0,
    skipped_cases INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    duration_ms BIGINT,
    error_summary TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_trigger_type CHECK (trigger_type IN ('MANUAL', 'CRON', 'API')),
    CONSTRAINT ck_run_status CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS t_test_case_result (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES t_test_run(id) ON DELETE CASCADE,
    case_id BIGINT NOT NULL REFERENCES t_test_case(id) ON DELETE RESTRICT,
    suite_case_id BIGINT REFERENCES t_test_suite_case(id) ON DELETE SET NULL,
    step_no INTEGER,
    step_name VARCHAR(128),
    status VARCHAR(16) NOT NULL,
    response_time_ms INTEGER,
    assertion_failed INTEGER NOT NULL DEFAULT 0,
    failed_assert_expr TEXT,
    request_snapshot JSONB,
    response_snapshot JSONB,
    extracted_vars JSONB,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_result_status CHECK (status IN ('PASSED', 'FAILED', 'SKIPPED', 'ERROR'))
);

CREATE TABLE IF NOT EXISTS t_test_plan (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES t_project(id) ON DELETE CASCADE,
    plan_name VARCHAR(128) NOT NULL,
    suite_id BIGINT NOT NULL REFERENCES t_test_suite(id) ON DELETE RESTRICT,
    environment_id BIGINT NOT NULL REFERENCES t_test_environment(id) ON DELETE RESTRICT,
    cron_expr VARCHAR(128) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
    last_run_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (project_id, plan_name),
    CONSTRAINT ck_plan_status CHECK (status IN ('ENABLED', 'DISABLED'))
);

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
