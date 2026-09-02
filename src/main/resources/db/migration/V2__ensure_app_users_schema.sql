-- Recovery migration: ensures app_users exists when V1 was skipped by baseline
CREATE TABLE IF NOT EXISTS app_users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_on  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_users_username UNIQUE (username)
);

CREATE INDEX IF NOT EXISTS idx_app_users_username ON app_users (username);

-- Recreate refresh_tokens if it was linked to the old users table
DROP TABLE IF EXISTS refresh_tokens CASCADE;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    token       VARCHAR(512) NOT NULL,
    user_id     BIGINT       NOT NULL REFERENCES app_users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_refresh_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens (user_id);

-- Drop legacy broken table if present
DROP TABLE IF EXISTS users CASCADE;
