CREATE TABLE IF NOT EXISTS memory_photo (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    memory_label VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_memory_photo_owner ON memory_photo(owner_user_id);

CREATE TABLE IF NOT EXISTS memory_quiz_question (
    id UUID PRIMARY KEY,
    memory_photo_id UUID NOT NULL,
    question VARCHAR(500) NOT NULL,
    options_json VARCHAR(1000) NOT NULL,
    correct_option_index INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_memory_quiz_question_photo ON memory_quiz_question(memory_photo_id);

CREATE TABLE IF NOT EXISTS memory_quiz_attempt (
    id UUID PRIMARY KEY,
    memory_photo_id UUID NOT NULL,
    respondent_user_id UUID NOT NULL,
    correct_count INT NOT NULL,
    total_count INT NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_memory_quiz_attempt_photo ON memory_quiz_attempt(memory_photo_id);
