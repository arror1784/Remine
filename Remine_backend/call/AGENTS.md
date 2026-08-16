<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# call

## Purpose
Owns the call log domain: starting a call between a parent/child pair, ending it, and reading back call history and monthly usage stats for a user. A call log records who called whom and when, tracked through `CallStatus` (`CONNECTING` → `CONNECTED`/`ENDED`/`MISSED`); this module does not handle real-time signaling/media (WebRTC, SFU, etc.) — it is purely the log/record-keeping side of a call feature, with `StartCallService`/`EndCallService` computing status transitions and duration. Callee resolution for parent-initiated calls without an explicit target uses the authenticated principal's paired counterpart (`RemineUserPrincipal.counterpartUserId()`/`.parentUserId()` from the `auth` module).

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/call/domain/CallLog.kt` | Core domain entity (immutable data class): id, callerId, calleeId, status, startedAt/endedAt, durationSeconds |
| `src/main/kotlin/com/remine/call/domain/CallStatus.kt` | Enum `CONNECTING`/`CONNECTED`/`ENDED`/`MISSED` with case-insensitive `from(name)` parser |
| `src/main/kotlin/com/remine/call/domain/CallStats.kt` | Aggregate value object (count, totalDurationSeconds) returned by stats queries |
| `src/main/kotlin/com/remine/call/application/port/inbound/StartCallCommand.kt` | CQRS inbound command port: `In(callerId, calleeId)` → `Out(entity: CallLog)` |
| `src/main/kotlin/com/remine/call/application/port/inbound/EndCallCommand.kt` | CQRS inbound command port: `In(callId, endedByUserId)` → `Out(entity: CallLog)` |
| `src/main/kotlin/com/remine/call/application/port/inbound/GetCallHistoryQuery.kt` | CQRS inbound query port: `In(userId, limit = 20)` → `Out(items: List<CallLog>)` |
| `src/main/kotlin/com/remine/call/application/port/inbound/GetCallStatsQuery.kt` | CQRS inbound query port: `In(userId, sinceMonthStart: LocalDate)` → `Out(count, totalDurationSeconds)` |
| `src/main/kotlin/com/remine/call/application/port/outbound/CallLogRepositoryPort.kt` | Outbound port the JPA adapter implements: `save`, `findById`, `findHistoryByUserId`, `getCallStats` |
| `src/main/kotlin/com/remine/call/application/service/StartCallService.kt` | Creates a new `CallLog` in `CONNECTING` status |
| `src/main/kotlin/com/remine/call/application/service/EndCallService.kt` | Loads the call, enforces caller-or-callee-only via `ForbiddenException`, computes duration, sets `ENDED` |
| `src/main/kotlin/com/remine/call/application/service/GetCallHistoryService.kt` | Read-only history lookup |
| `src/main/kotlin/com/remine/call/application/service/GetCallStatsService.kt` | Read-only stats lookup, converts `sinceMonthStart` (LocalDate) to an `Instant` at UTC midnight |
| `src/main/kotlin/com/remine/call/adapter/infrastructure/jpa/CallLogJpaEntity.kt` | JPA entity extending `BaseOrmEntity`; `@Where(clause = "deleted_at IS NULL")` for soft-delete; `toDomain()`/`fromDomain()` mappers |
| `src/main/kotlin/com/remine/call/adapter/infrastructure/jpa/CallLogJpaRepository.kt` | Spring Data repository with two `@Query` JPQL methods for history and aggregated stats |
| `src/main/kotlin/com/remine/call/adapter/infrastructure/jpa/CallLogRepositoryAdapter.kt` | Implements `CallLogRepositoryPort`; `save()` does an update-if-exists/insert-otherwise merge against the JPA entity |
| `src/main/kotlin/com/remine/call/adapter/presentation/web/CallController.kt` | REST controller at `/api/v1/calls` |
| `src/main/kotlin/com/remine/call/adapter/presentation/web/CallResponse.kt` | Outbound DTO with `from(CallLog)` mapper |
| `src/main/kotlin/com/remine/call/adapter/presentation/web/CallStatsResponse.kt` | Outbound DTO for stats endpoint |
| `src/main/kotlin/com/remine/call/adapter/presentation/web/StartCallRequest.kt` | Inbound DTO; `calleeId` is optional (omitted → derive from paired principal) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/call/domain` | Entities/VOs/enums, no framework dependencies |
| `src/main/kotlin/com/remine/call/application/port/inbound` | CQRS Command/Query interfaces with In/Out inner classes |
| `src/main/kotlin/com/remine/call/application/port/outbound` | Repository port interface implemented by the JPA adapter |
| `src/main/kotlin/com/remine/call/application/service` | Command/Query handler implementations (`@Service`, `@Transactional`) |
| `src/main/kotlin/com/remine/call/adapter/infrastructure/jpa` | JPA entity, Spring Data repository, and the adapter bridging port ↔ JPA |
| `src/main/kotlin/com/remine/call/adapter/presentation/web` | REST controller and request/response DTOs |
| `src/test/kotlin/com/remine/call/adapter/presentation/web` | Controller-level tests |
| `src/test/kotlin/com/remine/call/application/service` | Service-level tests against an in-memory fake port |

## For AI Agents

### Working In This Directory
- REST endpoints live under `/api/v1/calls` (`CallController.kt`): `POST /` (start), `PATCH /{id}/end` (end), `GET /` (history, `limit` query param), `GET /stats` (current-month aggregate). Verb usage already follows the project convention (POST=create, PATCH=update, GET=read); do not introduce a `PUT`.
- Every new Command/Query must follow the existing pattern exactly: one interface with a `fun handle(x: In): Out`, plus nested `In`/`Out` data classes — see any file in `application/port/inbound` as the template.
- `CallLogJpaEntity` extends `common`'s `BaseOrmEntity` and re-declares `@Where(clause = "deleted_at IS NULL")` itself — `BaseOrmEntity` does not propagate that annotation to subclasses (Hibernate 5.6 limitation, documented in `common`). Any new JPA entity in this module must do the same.
- No DB foreign keys: `callerId`/`calleeId` are plain `UUID` columns with no FK constraint, matching the project-wide "ID + index only" rule — do not add `@ManyToOne`/FK constraints when extending this module.
- `EndCallService` is the place authorization for "who can end a call" lives (caller or callee only, else `ForbiddenException`) — this is business-rule authorization, distinct from Spring Security's `@PreAuthorize`, which is not used in this module's controller.
- This module has no Flyway migrations of its own in this directory listing — check the shared migration location before adding/renaming columns on `call_log`.

### Testing Requirements
- `src/test/kotlin/com/remine/call/adapter/presentation/web/CallControllerTest.kt` — instantiates `CallController` directly with hand-written fake Command/Query implementations (no Spring context, no MockMvc); covers parent/child callee-routing logic and each endpoint's happy path.
- `src/test/kotlin/com/remine/call/application/service/CallServiceTest.kt` — exercises all four services against an in-memory `MockCallLogRepositoryPort` fake; covers start/end lifecycle, `ForbiddenException`/`EntityNotFoundException` cases, and history/stats aggregation.
- Both test files use plain JUnit 5 (`org.junit.jupiter`) with hand-rolled fakes rather than Mockito/MockK — follow that style for new tests in this module.

### Common Patterns
- Domain (`CallLog`) is an immutable `data class` with sensible defaults (`id = UUID.randomUUID()`, `status = CallStatus.CONNECTING`); services build updated state via `.copy(...)` rather than mutation.
- The JPA adapter's `save()` manually checks `findById(...).orElse(null)` to decide update-vs-insert instead of relying on Hibernate's dirty checking across the port boundary — keep this pattern for any new outbound methods that both create and update.
- Read services are annotated `@Transactional(readOnly = true)`; write services are annotated `@Transactional` — preserve this distinction for new services.

## Dependencies

### Internal
- `common` (`api` dependency) — `BaseOrmEntity`, domain exceptions (`EntityNotFoundException`, `ForbiddenException`), `ApiResponse`
- `auth` (`implementation`) — `RemineUserPrincipal`, `Role` used by the web controller for principal-based callee resolution

### External
- `spring-boot-starter-data-jpa` — JPA entity/repository support
- `spring-boot-starter-web` — REST controller support
- `spring-boot-starter-validation` — bean validation (not yet visibly used on DTOs, but on the classpath)
- `spring-boot-starter-security` — `@AuthenticationPrincipal` support in the controller
- `com.h2database:h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
