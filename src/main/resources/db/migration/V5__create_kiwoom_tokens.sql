CREATE TABLE IF NOT EXISTS kiwoom_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_type VARCHAR(50) NOT NULL,
    token VARCHAR(2000) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    expires_dt VARCHAR(14) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
