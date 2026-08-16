CREATE TABLE IF NOT EXISTS daily_activity_stat (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    sleep_minutes INT NOT NULL DEFAULT 0,
    steps INT NOT NULL DEFAULT 0,
    outing_count INT NOT NULL DEFAULT 0,
    social_contact_count INT NOT NULL DEFAULT 0,
    sleep_goal_minutes INT NOT NULL DEFAULT 480,
    steps_goal INT NOT NULL DEFAULT 8000,
    outing_goal INT NOT NULL DEFAULT 1,
    social_goal INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_daily_activity_stat_user_date ON daily_activity_stat(user_id, stat_date);

CREATE TABLE IF NOT EXISTS activity_checklist_item (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL,
    done BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    note VARCHAR(200),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_activity_checklist_user_date_type ON activity_checklist_item(user_id, stat_date, type);

CREATE TABLE IF NOT EXISTS activity_timeline_event (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    label VARCHAR(200) NOT NULL,
    color_hint VARCHAR(16),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_activity_timeline_user_date ON activity_timeline_event(user_id, stat_date);

CREATE TABLE IF NOT EXISTS activity_cheer (
    id UUID PRIMARY KEY,
    checklist_item_id UUID NOT NULL,
    sender_user_id UUID NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_activity_cheer_item ON activity_cheer(checklist_item_id);
