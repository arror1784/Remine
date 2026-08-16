CREATE TABLE IF NOT EXISTS memory_quiz_draft_question (
    id UUID PRIMARY KEY,
    memory_photo_id UUID NOT NULL,
    question VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_memory_quiz_draft_question_photo ON memory_quiz_draft_question(memory_photo_id);
