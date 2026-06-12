-- =============================================
--  Reviora Database Schema V1
--  Initial migration — all core tables
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─── users ──────────────────────────────────
CREATE TABLE users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(255)        NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    picture       VARCHAR(500),
    provider      VARCHAR(20)         NOT NULL DEFAULT 'LOCAL',
    provider_id   VARCHAR(255),
    role          VARCHAR(20)         NOT NULL DEFAULT 'USER',
    is_active     BOOLEAN             NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

-- ─── submissions ────────────────────────────
CREATE TABLE submissions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    code            TEXT                NOT NULL,
    language        VARCHAR(30)         NOT NULL,
    stdin           TEXT,
    status          VARCHAR(20)         NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_submissions_user_id     ON submissions(user_id);
CREATE INDEX idx_submissions_created_at  ON submissions(created_at DESC);
CREATE INDEX idx_submissions_language    ON submissions(language);

-- ─── execution_results ──────────────────────
CREATE TABLE execution_results (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id    UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    stdout           TEXT,
    stderr           TEXT,
    exit_code        INTEGER             NOT NULL DEFAULT 0,
    execution_time   INTEGER             NOT NULL DEFAULT 0,  -- ms
    memory_used      BIGINT,                                   -- bytes
    created_at       TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exec_results_submission_id ON execution_results(submission_id);

-- ─── complexity_reports ─────────────────────
CREATE TABLE complexity_reports (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id       UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    time_complexity     VARCHAR(30)         NOT NULL,
    space_complexity    VARCHAR(30)         NOT NULL DEFAULT 'O(1)',
    pattern             VARCHAR(50),
    confidence          NUMERIC(4,2)        DEFAULT 0.85,
    explanation         TEXT,
    nesting_depth       INTEGER             DEFAULT 0,
    loop_count          INTEGER             DEFAULT 0,
    recursion_detected  BOOLEAN             DEFAULT FALSE,
    created_at          TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_complexity_submission_id ON complexity_reports(submission_id);

-- ─── growth_data ────────────────────────────
CREATE TABLE growth_data (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id    UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    input_size       INTEGER NOT NULL,
    operations       BIGINT  NOT NULL,
    execution_time_ms NUMERIC(10,3) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_growth_data_submission_id ON growth_data(submission_id);

-- ─── ai_reviews ─────────────────────────────
CREATE TABLE ai_reviews (
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id            UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    current_approach         TEXT,
    suggested_optimization   TEXT,
    expected_complexity      VARCHAR(30),
    interview_notes          TEXT,
    alternative_approaches   TEXT[],
    code_quality_score       NUMERIC(3,1)    DEFAULT 5.0,
    created_at               TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_reviews_submission_id ON ai_reviews(submission_id);

-- ─── test_cases ─────────────────────────────
CREATE TABLE test_cases (
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    submission_id    UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    input            TEXT,
    expected_output  TEXT,
    description      VARCHAR(255),
    category         VARCHAR(20)     NOT NULL DEFAULT 'normal',
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_test_cases_submission_id ON test_cases(submission_id);

-- ─── user_analytics ─────────────────────────
CREATE TABLE user_analytics (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id                     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_submissions           INTEGER     NOT NULL DEFAULT 0,
    accepted_submissions        INTEGER     NOT NULL DEFAULT 0,
    current_streak              INTEGER     NOT NULL DEFAULT 0,
    longest_streak              INTEGER     NOT NULL DEFAULT 0,
    last_submission_date        DATE,
    average_complexity_score    NUMERIC(4,2) DEFAULT 0,
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id)
);

CREATE INDEX idx_user_analytics_user_id ON user_analytics(user_id);

-- ─── topic_performance ──────────────────────
CREATE TABLE topic_performance (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic       VARCHAR(50) NOT NULL,
    solved      INTEGER     NOT NULL DEFAULT 0,
    total       INTEGER     NOT NULL DEFAULT 0,
    score       NUMERIC(5,2) DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, topic)
);

CREATE INDEX idx_topic_perf_user_id ON topic_performance(user_id);

-- ─── refresh_tokens ─────────────────────────
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ─── Trigger: update updated_at automatically ─
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at       BEFORE UPDATE ON users       FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_submissions_updated_at BEFORE UPDATE ON submissions  FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
