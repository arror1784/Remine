CREATE TABLE IF NOT EXISTS chat_message (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    body VARCHAR(1000) NOT NULL,
    quick_reply_key VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_chat_message_sender ON chat_message(sender_id);
CREATE INDEX IF NOT EXISTS ix_chat_message_recipient ON chat_message(recipient_id);

CREATE TABLE IF NOT EXISTS quick_reply (
    id UUID PRIMARY KEY,
    role VARCHAR(10) NOT NULL,
    label VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
