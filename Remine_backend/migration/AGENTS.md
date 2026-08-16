<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# migration

## Purpose
`migration` is a resource-only Gradle module (no Kotlin source) that holds every Flyway migration file for the whole system, under `src/main/resources/db/migration`. It has no hexagonal layering — it exists purely so the SQL files land on `app-api`'s runtime classpath when Flyway runs at boot. As of this writing it holds 11 migrations spanning users, notifications, messaging, calls, memories/quizzes, activity tracking, family posts, and demo-account seeding.

## Key Files
| File | Description |
|------|-------------|
| `src/main/resources/db/migration/V1__create_user_tables.sql` | Creates `app_user` (role, name, age_group, interests, email, google_id, invite_code, paired_user_id, streak_days) with indexes on paired_user_id, invite_code, google_id |
| `src/main/resources/db/migration/V2__create_notification_tables.sql` | Creates `notification` (recipient_user_id, emoji, bg_color, title, description, deep_link, read) with index on recipient_user_id |
| `src/main/resources/db/migration/V3__create_message_tables.sql` | Creates `chat_message` (sender_id, recipient_id, body, quick_reply_key) and `quick_reply` (role, label, sort_order) |
| `src/main/resources/db/migration/V4__create_call_tables.sql` | Creates `call_log` (caller_id, callee_id, status, started_at, ended_at, duration_seconds) |
| `src/main/resources/db/migration/V5__create_memory_tables.sql` | Creates `memory_photo`, `memory_quiz_question` (options_json, correct_option_index), and `memory_quiz_attempt` (correct_count, total_count) |
| `src/main/resources/db/migration/V6__create_activity_tables.sql` | Creates `daily_activity_stat` (sleep/steps/outing/social counts + goals), `activity_checklist_item`, `activity_timeline_event`, and `activity_cheer` |
| `src/main/resources/db/migration/V7__create_family_tables.sql` | Creates `family_post` (body, photo_url, like_count), `family_post_like`, and `family_post_reply` |
| `src/main/resources/db/migration/V8__seed_demo_users.sql` | Idempotently inserts two fixed-UUID demo accounts (parent + paired child) for the zero-friction `/api/v1/auth/demo-login` flow; UUIDs are duplicated in `user`'s `DemoLoginService` and must stay in sync |
| `src/main/resources/db/migration/V9__rename_demo_users.sql` | Follow-up `UPDATE` renaming the two demo accounts' `name` columns — added as a new migration rather than editing V8, since V8 was already applied |
| `src/main/resources/db/migration/V10__create_memory_quiz_draft_question_table.sql` | Creates `memory_quiz_draft_question` (question, sort_order) keyed by memory_photo_id |
| `src/main/resources/db/migration/V11__create_daily_activity_recommendation_table.sql` | Creates `daily_activity_recommendation` (parent_message, child_message, action_type) keyed by user_id + stat_date |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/resources/db/migration/` | All Flyway-versioned SQL migration files, in `V{n}__description.sql` format |

## For AI Agents

### Working In This Directory
- **Naming convention**: `V{n}__snake_case_description.sql`, where `{n}` is a strictly increasing integer (currently 1–11, no gaps). Flyway sorts and applies these in numeric order. Always pick the next unused integer — do not reuse or renumber existing versions.
- **Always additive, never edit a committed/applied migration.** Once a `V{n}__*.sql` file has been applied to any environment (including a teammate's local DB), it is immutable — Flyway checksums it. If you need to change something a prior migration did (as with V9 renaming what V8 seeded), write a new migration with the next version number rather than touching the old file.
- **Idempotent DDL only**: every `CREATE TABLE` uses `IF NOT EXISTS`, every `CREATE INDEX` uses `IF NOT EXISTS`. Seed/data migrations (like V8) guard inserts with a `WHERE NOT EXISTS (...)` check against the primary key so re-running the migration set (e.g. against a fresh H2 dev DB) is safe.
- **No DB foreign-key constraints** — every relationship (e.g. `recipient_user_id`, `memory_photo_id`, `checklist_item_id`) is a plain UUID column with a supporting `CREATE INDEX`, never a `REFERENCES` clause. This matches the system-wide invariant of relating by ID + index only.
- **Every table carries the soft-delete/audit columns** `created_at TIMESTAMP NOT NULL`, `updated_at TIMESTAMP NOT NULL`, `deleted_at TIMESTAMP` (nullable) — this is the SQL-level mirror of the common base entity (`BaseOrmEntity`) that JPA entities in other modules extend. Never model "deleted" as an `is_active` boolean.
- **Prod runs `ddl-auto=validate`.** Any new/changed JPA entity in another module must be paired with a migration here in the same change, or the app will fail to boot in prod (Hibernate validates the entity mapping against the actual schema).
- This module has no Kotlin/Spring code and no hexagonal layers (`domain`/`application`/`adapter`) — it is intentionally flat.

### Testing Requirements
No tests exist for this module (it has no `src/test`). Migrations are implicitly verified whenever any other module's Spring context loads against the dev H2 DB or CI runs against Postgres, since Flyway validates and applies them at startup.

### Common Patterns
- Table names are singular snake_case (`app_user`, `notification`, `chat_message`, `call_log`, `memory_photo`, `daily_activity_stat`, `family_post`), matching the domain concept the owning module models.
- Foreign-key-shaped columns are named `<entity>_id` (e.g. `recipient_user_id`, `owner_user_id`, `checklist_item_id`) and always get a `CREATE INDEX IF NOT EXISTS ix_<table>_<column(s)>` immediately after the table definition.
- Enum-like columns (`role`, `status`, `action_type`) are stored as `VARCHAR` with an application-level enum, not a SQL `CHECK`/enum type.
- A single migration file may create multiple related tables when they're introduced together for one feature (see V3, V5, V6, V7) rather than one file per table.

## Dependencies

### Internal
None — this module only supplies resource files. `app-api` (or equivalent bootJar module) depends on `migration` to load the SQL onto its runtime classpath.

### External
- Flyway (applied implicitly via Spring Boot's `spring-boot-starter-flyway` integration in the consuming app module; this module itself declares no plugin/library dependencies beyond the Kotlin/Gradle plugin scaffolding)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
