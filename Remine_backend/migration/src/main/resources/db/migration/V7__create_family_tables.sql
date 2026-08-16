CREATE TABLE IF NOT EXISTS family_post (
    id UUID PRIMARY KEY,
    author_user_id UUID NOT NULL,
    body VARCHAR(1000) NOT NULL,
    photo_url VARCHAR(500),
    photo_caption VARCHAR(200),
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_family_post_author ON family_post(author_user_id);

CREATE TABLE IF NOT EXISTS family_post_like (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_family_post_like_post_user ON family_post_like(post_id, user_id);

CREATE TABLE IF NOT EXISTS family_post_reply (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    author_user_id UUID NOT NULL,
    body VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_family_post_reply_post ON family_post_reply(post_id);
