CREATE TABLE IF NOT EXISTS app_users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_on  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_app_users_username UNIQUE (username)
);

CREATE INDEX IF NOT EXISTS idx_app_users_username ON app_users (username);

CREATE TABLE IF NOT EXISTS product (
    id           BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    created_by   VARCHAR(100) NOT NULL,
    created_on   TIMESTAMP    NOT NULL,
    modified_by  VARCHAR(100),
    modified_on  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_product_name ON product (product_name);
CREATE INDEX IF NOT EXISTS idx_product_created_by ON product (created_by);

CREATE TABLE IF NOT EXISTS item (
    id         BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    quantity   INT    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_item_product_id ON item (product_id);

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
