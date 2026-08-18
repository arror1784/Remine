-- Second seed-account pair for the DEMO variant of /api/v1/auth/demo-login
-- (live demos), kept separate from V8's EVAL pair (AI product review) so demo
-- data can be wiped/reseeded via POST /api/v1/admin/demo/reset without ever
-- touching the EVAL accounts. The UUIDs below are duplicated in
-- DemoLoginService.SHOW_PARENT_ID / SHOW_CHILD_ID; keep both in sync.
INSERT INTO app_user (
    id, role, name, age_group, interests, email, google_id,
    invite_code, paired_user_id, streak_days, created_at, updated_at, deleted_at
)
SELECT
    CAST('7b2f4b0a-6e6c-4f3d-9c1a-2f6a5e9d7c31' AS UUID),
    'PARENT',
    '시연 부모',
    '70대',
    '걷기·산책,수면 관리,가족 소통',
    NULL,
    NULL,
    'REMIND-SHOW',
    CAST('d4a8c6f2-1b3e-4a5d-8f7c-3e9b2a6d4f18' AS UUID),
    3,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
FROM (VALUES (1)) AS seed_guard(dummy)
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE id = CAST('7b2f4b0a-6e6c-4f3d-9c1a-2f6a5e9d7c31' AS UUID)
);

INSERT INTO app_user (
    id, role, name, age_group, interests, email, google_id,
    invite_code, paired_user_id, streak_days, created_at, updated_at, deleted_at
)
SELECT
    CAST('d4a8c6f2-1b3e-4a5d-8f7c-3e9b2a6d4f18' AS UUID),
    'CHILD',
    '시연 자녀',
    '40대',
    NULL,
    NULL,
    NULL,
    NULL,
    CAST('7b2f4b0a-6e6c-4f3d-9c1a-2f6a5e9d7c31' AS UUID),
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL
FROM (VALUES (1)) AS seed_guard(dummy)
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE id = CAST('d4a8c6f2-1b3e-4a5d-8f7c-3e9b2a6d4f18' AS UUID)
);
