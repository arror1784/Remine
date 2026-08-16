-- V8 seeded the demo accounts with generic placeholder names. Renaming them
-- here (rather than editing V8) since V8 is an already-applied migration —
-- Flyway migrations are never edited after being applied, only followed up.
UPDATE app_user SET name = '윤정아' WHERE id = CAST('1c77b040-9278-4a22-adb1-0345ab254551' AS UUID);
UPDATE app_user SET name = '지영' WHERE id = CAST('01421a39-6467-465c-a6e5-8e3007225296' AS UUID);
