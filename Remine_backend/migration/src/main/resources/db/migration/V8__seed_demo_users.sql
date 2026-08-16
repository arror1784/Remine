-- Demo accounts for the zero-friction demo login (/api/v1/auth/demo-login).
-- The UUIDs below are duplicated in DemoLoginService; keep both in sync.
INSERT INTO app_user (
    id, role, name, age_group, interests, email, google_id,
    invite_code, paired_user_id, streak_days, created_at, updated_at, deleted_at
)
SELECT
    CAST('1c77b040-9278-4a22-adb1-0345ab254551' AS UUID),
    'PARENT',
    '데모 부모',
    '70대',
    '걷기·산책,수면 관리,가족 소통',
    NULL,
    NULL,
    'REMIND-DEMO',
    CAST('01421a39-6467-465c-a6e5-8e3007225296' AS UUID),
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
FROM (VALUES (1)) AS seed_guard(dummy)
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE id = CAST('1c77b040-9278-4a22-adb1-0345ab254551' AS UUID)
);

INSERT INTO app_user (
    id, role, name, age_group, interests, email, google_id,
    invite_code, paired_user_id, streak_days, created_at, updated_at, deleted_at
)
SELECT
    CAST('01421a39-6467-465c-a6e5-8e3007225296' AS UUID),
    'CHILD',
    '데모 자녀',
    '40대',
    NULL,
    NULL,
    NULL,
    NULL,
    CAST('1c77b040-9278-4a22-adb1-0345ab254551' AS UUID),
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
FROM (VALUES (1)) AS seed_guard(dummy)
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE id = CAST('01421a39-6467-465c-a6e5-8e3007225296' AS UUID)
);
