<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# activity

## Purpose
Owns the parent's daily life-activity tracking: recorded sleep/steps/outing/social-contact stats against per-user goals, a derived daily checklist (SLEEP/BREAKFAST/WALK/QUIZ), children sending "cheers" on completed checklist items, a same-day event timeline, and an AI-generated daily activity recommendation (parent-facing encouragement message + child-facing status message + a suggested `actionType` of WALK/CALL/QUIZ/NONE). This is the data source behind the "일상 분석 & AI 코칭" feature described in the root CLAUDE.md service overview.

## Key Files
| File | Description |
|------|-------------|
| `domain/DailyActivityStat.kt` | Per-user-per-day stat record (sleep/steps/outing/social) plus per-metric goals, defaults 480min sleep / 8000 steps / 1 outing / 1 social contact |
| `domain/ActivityChecklistItem.kt` | One daily checklist row (`type: String`, `done`, `completedAt`, `note`) |
| `domain/ActivityCheer.kt` | A child's cheer sent on a checklist item, one per sender per item per day |
| `domain/ActivityTimelineEvent.kt` | A labeled, timestamped event on a given `statDate` |
| `domain/DailyActivityRecommendation.kt` | Cached AI-generated recommendation for a user+date, with `actionType` |
| `domain/DailyActivityRecommendationActionType.kt` | Enum `WALK / CALL / QUIZ / NONE`, with `fromStringOrNull` for parsing AI JSON output |
| `application/service/DailyActivityService.kt` | Implements `RecordDailyActivityCommand`, `UpdateDailyActivityCommand`, `SyncDailyActivityCommand`, `GetTodaySummaryQuery`, `GetWeeklyPatternQuery` |
| `application/service/ActivityChecklistService.kt` | Implements `ToggleChecklistItemCommand`, `SendCheerCommand`, `GetChecklistQuery`; lazily creates the 4 default checklist items on first read of a date |
| `application/service/ActivityTimelineService.kt` | Implements `RecordTimelineEventCommand`, `GetTimelineQuery` |
| `application/service/DailyActivityRecommendationService.kt` | Implements `GetDailyActivityRecommendationQuery`; cache-or-generate-then-save against `ActivityRecommendationGeneratorPort` |
| `application/port/outbound/ActivityRecommendationGeneratorPort.kt` | Outbound port to the AI recommendation generator, returns `GeneratedRecommendation(parentMessage, childMessage, actionType)` |
| `adapter/infrastructure/ai/OpenAiActivityRecommendationGenerator.kt` | Implements the port via `client-openai`'s `OpenAiClient.completeJson`; builds Korean system/user prompts and parses the returned JSON |
| `adapter/presentation/web/ActivityController.kt` | REST controller, base path `/api/v1/activities` |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `domain/` | Entities and the recommendation-action enum; no Spring/JPA dependencies |
| `application/service/` | CQRS command/query handler implementations |
| `application/port/inbound/` | One interface per Command/Query, each with nested `In`/`Out` |
| `application/port/outbound/` | Repository ports (`DailyActivityStatRepositoryPort`, `ActivityChecklistItemRepositoryPort`, `ActivityCheerRepositoryPort`, `ActivityTimelineEventRepositoryPort`, `DailyActivityRecommendationRepositoryPort`) and `ActivityRecommendationGeneratorPort` |
| `adapter/presentation/web/` | `ActivityController` plus one request/response DTO per file |
| `adapter/infrastructure/jpa/` | One JPA entity + Spring Data repository + `RepositoryAdapter` per aggregate (stat, checklist item, cheer, timeline event, recommendation) |
| `adapter/infrastructure/ai/` | `OpenAiActivityRecommendationGenerator`, the only outbound-port implementation calling an external AI provider from this module |

## For AI Agents

### Working In This Directory
- All REST routes live under `/api/v1/activities` in a single `ActivityController`; every handler resolves the acting parent via `principal.parentUserId()` (from the `auth` module), so a CHILD-role caller acts on their paired parent's data, not their own userId.
- `RecordDailyActivityCommand` throws `InvalidRequestException` if a stat already exists for that `userId`+`statDate` — `PATCH /{statDate}` (`UpdateDailyActivityCommand`) is the correct path for edits, not re-POSTing.
- `PATCH /sync` (`SyncDailyActivityCommand`) is the bulk endpoint per root CLAUDE.md's "single `PATCH /<resource>/sync`" rule — it upserts a batch of daily entries in one call via `findByUserIdAndStatDateIn` + `saveAll`, no per-row round trips.
- `GetChecklistQuery` has a side effect: reading a checklist for a date that has none yet creates and persists the 4 default items (SLEEP/BREAKFAST/WALK/QUIZ). Don't assume queries here are side-effect-free.
- `SendCheerCommand` silently no-ops (`Out(entity = null)`) if the same sender already cheered the same checklist item today, rather than throwing — callers must handle a null `ActivityCheerResponse`.
- `DailyActivityRecommendationService` caches the AI-generated recommendation per `userId`+`statDate` in `DailyActivityRecommendationRepositoryPort`; it only calls out to `ActivityRecommendationGeneratorPort` (OpenAI) when no cached row exists and a `DailyActivityStat` exists for that date, otherwise it returns an uncached, unsaved default `NONE` recommendation.
- Percent-of-goal math (`min(100, value*100/goal)`) is duplicated in `DailyActivityService.handle(GetTodaySummaryQuery.In)` and `DailyActivityRecommendationService.handle(...)` — keep both in sync if the formula changes.
- All JPA entities extend `common`'s `BaseOrmEntity` (id/createdAt/updatedAt/deletedAt) and use `@Where(clause = "deleted_at IS NULL")` for the soft-delete convention from root CLAUDE.md; there are no `@ManyToOne`/FK relations — `userId`/`checklistItemId` are plain indexed UUID columns.
- The AI prompt/response contract (Korean `parentMessage`/`childMessage`/`actionType` JSON) is defined entirely in `OpenAiActivityRecommendationGenerator` — changing the JSON shape requires updating both the system prompt string and `parseRecommendation`.

### Testing Requirements
- `src/test/kotlin/.../application/service/ActivityServiceTest.kt` — exercises `DailyActivityService` and `ActivityChecklistService` against in-memory fakes of the outbound ports (record/update/sync/today-summary/checklist/cheer flows, including the "already exists" and "not found"/"forbidden" error paths).
- `src/test/kotlin/.../application/service/DailyActivityRecommendationServiceTest.kt` — covers the cache-hit, no-stat-default, and generate-and-save paths for `DailyActivityRecommendationService`.
- `src/test/kotlin/.../adapter/presentation/web/ActivityControllerTest.kt` — controller-level tests asserting a CHILD-role principal's calls resolve against the paired parent's `userId`.
- `src/test/kotlin/.../adapter/infrastructure/ai/OpenAiActivityRecommendationGeneratorTest.kt` — uses a `StubOpenAiClient` subclass of `client-openai`'s `OpenAiClient` to test prompt construction and JSON-response parsing/error cases.
- All four test classes use hand-written in-memory port fakes / stubs rather than Spring context or mocking libraries — follow that pattern for new tests in this module.

### Common Patterns
- Each Command/Query inbound port is a standalone interface with `handle(In): Out` and nested `data class In` / `data class Out`, implemented by a `@Service @Transactional` class that groups related ports together (e.g. `DailyActivityService` implements five related ports at once).
- Domain entities are immutable `data class`es with `copy()`-based updates; JPA entities are separate mutable classes with `toDomain()` / `companion object fromDomain()` conversion, isolating JPA/Hibernate from `domain/`.

## Dependencies

### Internal
- `common` (api) — `BaseOrmEntity`, `ApiResponse`, shared domain exceptions
- `auth` — `RemineUserPrincipal`, `@AuthenticationPrincipal` resolution
- `client-openai` — `OpenAiClient` used by the AI recommendation adapter

### External
- `spring-boot-starter-data-jpa` — JPA entities/repositories
- `spring-boot-starter-web` — REST controller
- `spring-boot-starter-validation` — `@Valid` request DTOs
- `spring-boot-starter-security` — `@AuthenticationPrincipal`
- `com.h2database:h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
