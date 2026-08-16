# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

`Remine_backend/` and `Remine_frontend/` are currently empty — no source files, build tooling, or tests exist yet. The sections below describe the service being built and the intended stack/architecture, drawn from the project's planning docs (`서비스 가이드`, `백오피스-백엔드-스택-정리.md`, `백오피스-프론트엔드-스택-정리.md`). Treat this as the target design to scaffold toward, not a description of existing code. Once real code lands, update the "Commands" section below with actual build/lint/test/run commands and verify the architecture notes still match reality.

## Service overview

Remine is an AI-based dementia-prevention / cognitive-health service, aimed at middle-aged and older adults concerned about cognitive health — not a diagnostic tool and not a repetitive puzzle/quiz app. It uses a user's daily life data and personal memories (via AI) to drive personalized cognitive-health management:

- **일상 분석 & AI 코칭** — analyzes sleep, activity, outings, and social activity to recommend lifestyle/cognitive-health actions, comparing the user against their own baseline (not against others).
- **추억 기록 & 맞춤형 인지 활동** — family-registered old photos/memories, organized by year/theme, used by AI to generate personalized reminiscence questions and cognitive quizzes.
- **가족 공동 참여** — children/grandchildren add photos, memories, comments, and questions, turning cognitive activities into a shared family activity.

## Intended architecture

### Backend (`Remine_backend/`)

Kotlin + Spring Boot, hexagonal architecture (Ports & Adapters) + CQRS, split into Gradle multi-modules (per the reference project: app modules for `bootJar` targets like an API server and a worker, domain modules per business concern, and infra/common modules).

- Layers: `adapter → application → domain`, dependencies always point inward. Domain has no Spring/JPA/external dependencies.
- CQRS: Command and Query are separate inbound port interfaces, each with `In`/`Out` inner data classes.
- Standard module layout: `domain/` (entities, VOs, exceptions, events), `application/service` + `application/port/inbound` + `application/port/outbound`, `adapter/presentation/web` + `adapter/infrastructure/jpa`.
- External integrations (file storage, email, third-party sync, AI/knowledge proxy, etc.) go in dedicated `client-*` modules so a vendor/API change stays isolated from domain logic.
- DB: PostgreSQL (prod) / H2 (dev) via JPA/Hibernate, but **no DB foreign-key constraints** — relate by ID column + index only, to keep services decoupled and migrations flexible.
- Schema changes only via Flyway migrations (idempotent DDL, e.g. `IF NOT EXISTS`); prod runs `ddl-auto=validate`, so an entity change without a matching migration breaks boot.
- Redis for sessions, distributed locks, and a Stream-based task queue — if this queue is shared with a Python/AI service, keep stream keys/task types/fields in sync on both sides.
- Auth: Spring Security + JWT filter chain + Google OAuth; authorization via `@PreAuthorize` only (no hardcoded URL-path rules in filters).
- AI/knowledge processing (search, embeddings, RAG, STT) belongs in a dedicated Python service, not reimplemented in Kotlin — the backend only proxies to it. **Documented exception:** `client-openai` is a direct client for OpenAI's own Chat Completions API, not a Python-service proxy — this is an intentional, confirmed design choice for that module, not drift to fix.
- Secrets/config via Vault injection (`${ENV}`), never hardcoded in `application.yml` or source; when removing a feature, also remove the config/Vault keys that fed it.
- One class per file (except Command/Query `In`/`Out` inner classes).
- HTTP verbs: create=`POST`, update=`PATCH` (never `PUT`), delete=`DELETE`, read=`GET`.
- Bulk list saves get a single `PATCH /<resource>/sync` endpoint (`findAllByIds` + `saveAll` in one transaction) instead of the frontend calling per-row; deletes are not carried in `sync`.
- New entities must extend a common base entity providing `id`/`created_at`/`updated_at`/`deleted_at` (soft delete) — don't hand-roll `created_at` alone (easy to forget `deleted_at`), and don't model "deleted" as `is_active=false` (different meaning; caused ghost-data bugs where soft-deleted rows still appeared in some views).

### Frontend (`Remine_frontend/`)

React 18 + TypeScript, Vite build (`tsc -b && vite build` — build is also the type check), Yarn Classic for package management, Tailwind CSS for styling, Zustand for global state, react-router-dom for routing, Axios for HTTP (interceptors handle token attachment + 401 refresh), Chart.js for dashboard charts, Vitest for unit tests.

- **Single color source** — all colors defined once in a theme file as CSS variables; no hardcoded hex/`text-[#...]` in components. This is what makes light/dark theming a variable swap.
- **Shared components only** — build UI from common components (Button/Card/Table/PageHeader/etc.), don't restyle raw `<button>`/`<table>`/`<select>`.
- **ST/DR dual-buffer editing** — saved value (ST) vs. in-progress draft (DR) are kept separate; calculations/derived dashboard values always read ST, edits write DR, and DR→ST only happens on explicit save. Don't compute from DR or write edits directly to ST.
- **Domain API clients only** — components never call `axios`/`fetch` directly; go through `src/api/<domain>.ts` so auth/token-refresh stays centralized.
- **Backend contract uses camelCase**; always preserve the response `id` so CRUD stays in sync.
- **Chart.js instances must be cleaned up** before recreation to avoid memory leaks / ghost canvases.
- **Bulk sync, no N+1** — list saves send one `PATCH /sync` with only the changed rows, not one call per row.
- **Sidebar icons are registered in two places** — the nav data (`navigation.ts`) and the icon render mapping (`navIcons.ts`) — both need updating together.

### System-wide invariants (both sides)

- No DB foreign-key constraints anywhere (ID + index only).
- Secrets/config via Vault, never hardcoded.
- AI/knowledge processing lives in its own Python service; Kotlin backend only proxies — except `client-openai`, which is a confirmed, documented direct OpenAI client (see backend section above).
- Separate API namespaces for internal vs. externally-exposed vs. management-data routes.
- Schema changes only via Flyway migration, never manual DDL.

## Rule-loading layering (for this repo's own CLAUDE.md docs)

The reference project keeps root `CLAUDE.md` to system-wide invariants, then `backend/CLAUDE.md` / `frontend/CLAUDE.md` for that side's "always-loaded" absolute rules, with deeper detail (`architecture.md`, `module-structure.md`, `conventions.md`, etc.) living in `.claude/rules/*.md` and loaded only on demand, plus `.claude/skills/` runbooks for procedures like release/hotfix. As `Remine_backend/` and `Remine_frontend/` gain real code, consider splitting their own `CLAUDE.md` out from this root file the same way, so always-on rules stay short and deep detail is read only when relevant.
