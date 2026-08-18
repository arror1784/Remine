-- Remove any duplicate (user_id, stat_date) rows that may have accumulated from the
-- read-check-then-write race in DailyActivityRecommendationService before this fix,
-- keeping only the most recently created row per pair.
DELETE FROM daily_activity_recommendation
WHERE id NOT IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id, stat_date ORDER BY created_at DESC) AS rn
        FROM daily_activity_recommendation
    ) ranked
    WHERE ranked.rn = 1
);

DROP INDEX IF EXISTS ix_daily_activity_recommendation_user_date;

CREATE UNIQUE INDEX IF NOT EXISTS ux_daily_activity_recommendation_user_date
    ON daily_activity_recommendation(user_id, stat_date);
