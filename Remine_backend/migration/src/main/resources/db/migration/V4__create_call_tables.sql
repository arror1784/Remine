CREATE TABLE IF NOT EXISTS call_log (
    id UUID PRIMARY KEY,
    caller_id UUID NOT NULL,
    callee_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    duration_seconds INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_call_log_caller ON call_log(caller_id);
CREATE INDEX IF NOT EXISTS ix_call_log_callee ON call_log(callee_id);
