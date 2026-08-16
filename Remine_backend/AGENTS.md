<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# Remine_backend

## Purpose
Kotlin + Spring Boot backend, built as a Gradle multi-module project (`rootProject.name = "remine-backend"`) using hexagonal architecture (Ports & Adapters) plus CQRS. Twelve subprojects: one bootJar entry point (`app-api`), one shared kernel (`common`), one resource-only Flyway module (`migration`), one vendor-isolation client module (`client-openai`), and eight domain modules (`auth`, `user`, `notification`, `message`, `call`, `memory`, `activity`, `family`). No domain module is allowed to depend on another domain module directly — only `app-api` is allowed to see all of them, and cross-domain reads go through small aggregation endpoints hosted in `app-api` itself (see its own AGENTS.md).

## Key Files
| File | Description |
|------|-------------|
| `settings.gradle.kts` | Declares the 12 subprojects, in dependency order (`common`, `client-openai`, `migration` first; `app-api` last) |
| `build.gradle.kts` | Root Gradle config: Spring Boot 2.7.18, Kotlin 1.9.22, shared `subprojects {}` block (includes a documented workaround pinning `kotlin.version` before the Spring BOM import, to stop the BOM's older pinned Kotlin stdlib from winning) |
| `gradle/wrapper/` | Gradle wrapper |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `app-api/` | bootJar entry point; composes every domain module, hosts cross-domain aggregation endpoints and CORS/Redis config (see `app-api/AGENTS.md`) |
| `common/` | Shared kernel: `BaseOrmEntity` (soft-delete base entity), domain exception hierarchy, `ApiResponse<T>` envelope, global exception handler (see `common/AGENTS.md`) |
| `client-openai/` | Direct OpenAI Chat Completions API client, domain-agnostic (see `client-openai/AGENTS.md`) |
| `migration/` | Resource-only module holding every Flyway migration for the whole system (see `migration/AGENTS.md`) |
| `auth/` | JWT filter chain, token issuance/parsing, `RemineUserPrincipal`, role model, Google OAuth config (see `auth/AGENTS.md`) |
| `user/` | `User` aggregate: signup, profile, parent↔child pairing, demo-login (see `user/AGENTS.md`) |
| `notification/` | In-app single-recipient notifications: list, unread count, mark-as-read with same-`deepLink` clearing (see `notification/AGENTS.md`) |
| `message/` | 1:1 parent/child chat + quick-reply templates; sends real notifications on send (see `message/AGENTS.md`) |
| `call/` | Call-log record-keeping (not real-time signaling/media) (see `call/AGENTS.md`) |
| `memory/` | Memory photos + AI-generated reminiscence quizzes (3-step flow via `client-openai`) (see `memory/AGENTS.md`) |
| `activity/` | Daily activity stats, checklist, timeline, AI daily recommendation (see `activity/AGENTS.md`) |
| `family/` | Family feed: posts, likes, threaded replies, scoped to the resolved parent/child pair (see `family/AGENTS.md`) |
| `docs/` | Backend-specific planning/reference docs |

## For AI Agents

### Working In This Directory
Every new JPA entity anywhere in these modules must extend `common`'s `BaseOrmEntity` and must **redeclare** its own `@Where(deleted_at IS NULL)` soft-delete clause — that annotation does not propagate from a `@MappedSuperclass` on this project's Hibernate version, and forgetting it is a real, previously-hit bug class (see `common/AGENTS.md`). Never add a DB foreign-key constraint — relate by ID column + index only, per the root `CLAUDE.md` invariant, so services stay decoupled. Schema changes only ever go through a new Flyway migration file in `migration/`; prod runs `ddl-auto=validate`, so an entity change without a matching migration breaks boot at startup, not at request time. Follow the HTTP verb convention exactly: create=`POST`, update=`PATCH` (never `PUT`), delete=`DELETE`, read=`GET`; bulk list saves get one `PATCH /<resource>/sync` endpoint instead of per-row calls. Authorization is `@PreAuthorize` only — `SecurityConfig` in `auth` only gates authenticated-vs-not, never hardcode URL-path role rules in a filter.

### Testing Requirements
Run the full suite with `./gradlew test` from this directory, or `./gradlew :module-name:test` for a single module. Test coverage varies sharply by module: `memory`, `family`, `message`, `call`, `notification`, `activity` all have real service+controller test suites with hand-rolled in-memory port fakes (no Mockito); `app-api`, `client-openai`, `common`, `auth`, `user`, `migration` currently have none.

### Common Patterns
Every domain module follows the same internal shape: `domain/` (entities/VOs/exceptions, zero Spring/JPA imports), `application/port/inbound` (Command/Query interfaces, each with an `In`/`Out` inner data class) + `application/port/outbound` (repository/gateway interfaces) + `application/service` (one service class typically implementing one or more inbound ports), `adapter/presentation/web` (REST controllers) + `adapter/infrastructure/jpa` (JPA entities/repositories) and occasionally `adapter/infrastructure/ai` (AI-adapter implementations of an outbound port, proxying through `client-openai`) or `adapter/infrastructure/seed` (startup data seeders). Recipient/owner identity in every module is resolved server-side from the authenticated `RemineUserPrincipal`'s pairing (`parentUserId()`/`counterpartUserId()`), never trusted from the request body.

## Dependencies

### Internal
Dependency direction is strictly inward toward `common`: every domain module depends on `common` and typically `auth` (for `RemineUserPrincipal`); `memory` and `activity` additionally depend on `client-openai`; `message` calls into `notification` directly (not through an event bus) to fire real notifications on send; `app-api` depends on all of them.

### External
Spring Boot 2.7.18, Kotlin 1.9.22 (JVM target), Spring Data JPA/Hibernate, Flyway, PostgreSQL driver (prod) / H2 (dev), Spring Security, JJWT, Redis client, OpenAI HTTP client (in `client-openai` only).

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
