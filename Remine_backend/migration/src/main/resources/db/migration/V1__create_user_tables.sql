CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    role VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    age_group VARCHAR(10) NOT NULL,
    interests VARCHAR(255),
    email VARCHAR(255),
    google_id VARCHAR(255),
    invite_code VARCHAR(20),
    paired_user_id UUID,
    streak_days INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_app_user_paired_user_id ON app_user(paired_user_id);
CREATE INDEX IF NOT EXISTS ix_app_user_invite_code ON app_user(invite_code);
CREATE INDEX IF NOT EXISTS ix_app_user_google_id ON app_user(google_id);
