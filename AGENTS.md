<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# Remine

## Purpose
Remine is an AI-based dementia-prevention / cognitive-health service for middle-aged and older adults, built around two paired roles: a "parent" (부모님, the older adult being cared for) and a "child" (자녀, the family member managing/caring). It is not a diagnostic tool and not a repetitive puzzle app — it tracks daily-life activity (sleep, steps, outings, social contact) against the user's own baseline, turns family-submitted old photos into AI-generated reminiscence quizzes, and gives the paired family member a feed/chat/call channel to stay involved. This is a two-repo workspace: a Kotlin/Spring Boot backend and a React/TypeScript frontend, developed together against the conventions below.

## Key Files
| File | Description |
|------|-------------|
| `CLAUDE.md` | Canonical, always-loaded architecture/convention rules for both `Remine_backend/` and `Remine_frontend/` — read this before either subtree's own AGENTS.md |
| `서비스 가이드` | Korean product/service guide — what Remine is and why (원문 기획 문서) |
| `백오피스-백엔드-스택-정리.md` | Backend stack planning notes (Korean) |
| `백오피스-프론트엔드-스택-정리.md` | Frontend stack planning notes (Korean) |
| `.gitignore` | Standard ignores for both subprojects |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `Remine_backend/` | Kotlin + Spring Boot, Gradle multi-module, hexagonal architecture + CQRS (see `Remine_backend/AGENTS.md`) |
| `Remine_frontend/` | React 18 + TypeScript, Vite build (see `Remine_frontend/AGENTS.md`) |
| `.claude/` | Claude Code project-local settings — not application code |
| `버그 스크린샷/` | Ad-hoc bug screenshots (images only, not source) |

## For AI Agents

### Working In This Directory
This root has no source of its own — every real change happens under `Remine_backend/` or `Remine_frontend/`. Read `CLAUDE.md` first: it documents system-wide invariants that apply to *both* subtrees and must not be violated by a change scoped to just one side — no DB foreign-key constraints anywhere, secrets/config only via Vault (never hardcoded), AI/knowledge processing lives in its own Python service with Kotlin only proxying (verify against `Remine_backend/client-openai/AGENTS.md`, which documents one real exception to this rule), separate API namespaces per audience, and schema changes only via Flyway migration. When a feature spans both repos (e.g. a new notification type, a new activity metric), update the backend module, its Flyway migration, and the matching frontend `src/api/<domain>.ts` client together — they drift silently otherwise since there are no compile-time contracts between the two languages.

### Testing Requirements
Each subtree owns its own test suite — see `Remine_backend/AGENTS.md` (JVM/Kotlin tests per module, run via Gradle) and `Remine_frontend/AGENTS.md` (currently no test tooling installed). There is no root-level test runner.

### Common Patterns
Both sides mirror the same domain boundaries by name (`activity`, `family`, `memory`, `message`, `notification`, `user`, `call` on the backend; matching `src/api/<domain>.ts` clients and `src/pages/{parent,child}/<Domain>.tsx` screens on the frontend). When orienting in an unfamiliar area, match the domain name across both AGENTS.md trees rather than assuming file-layout symmetry.

## Dependencies

### Internal
N/A — this is the repository root.

### External
See `Remine_backend/AGENTS.md` (JVM/Gradle toolchain) and `Remine_frontend/AGENTS.md` (Node/Yarn toolchain) for the two independent dependency trees.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
