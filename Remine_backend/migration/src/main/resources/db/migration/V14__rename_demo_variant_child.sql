-- V9 already renamed the EVAL child (V8's seed pair) to '지영'. V13 seeded the
-- DEMO variant's child as a generic placeholder ('시연 자녀') that never got the
-- same treatment, so the two accounts showed different child names depending
-- on which one was logged into. Matching it here (not editing V13, which is
-- already applied) keeps both variants showing the same expected name.
UPDATE app_user SET name = '지영' WHERE id = CAST('d4a8c6f2-1b3e-4a5d-8f7c-3e9b2a6d4f18' AS UUID);
