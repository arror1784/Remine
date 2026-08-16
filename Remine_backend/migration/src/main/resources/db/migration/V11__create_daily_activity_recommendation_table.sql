CREATE TABLE IF NOT EXISTS daily_activity_recommendation (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stat_date DATE NOT NULL,
    parent_message VARCHAR(500) NOT NULL,
    child_message VARCHAR(500) NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_daily_activity_recommendation_user_date ON daily_activity_recommendation(user_id, stat_date);
