CREATE TABLE IF NOT EXISTS notification (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    emoji VARCHAR(8) NOT NULL,
    bg_color VARCHAR(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(500) NOT NULL,
    deep_link VARCHAR(255) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_notification_recipient ON notification(recipient_user_id);
