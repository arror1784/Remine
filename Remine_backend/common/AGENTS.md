<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# common

## Purpose
Shared kernel depended on by nearly every other module (`call` pulls it in via `api(project(":common"))`). It provides the one piece of infrastructure every JPA entity in the codebase must extend (`BaseOrmEntity`, giving `id`/`created_at`/`updated_at`/soft-delete `deleted_at`), the shared domain exception hierarchy that maps to HTTP status codes, the standard `ApiResponse<T>` response envelope, and the global `@RestControllerAdvice` exception handler that turns those exceptions into consistent JSON error bodies. Notably, `common` depends on `spring-security-core` (the core library only, not `spring-boot-starter-security`) specifically so `GlobalExceptionHandler` can catch Spring Security's `AccessDeniedException` without pulling in security auto-configuration into every module that depends on `common` — this is called out explicitly in `build.gradle.kts`.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/common/persistence/BaseOrmEntity.kt` | `@MappedSuperclass` every `*JpaEntity` extends: UUID `id`, auditing-managed `createdAt`/`updatedAt`, nullable `deletedAt` + `softDelete()`/`isDeleted` |
| `src/main/kotlin/com/remine/common/config/JpaAuditingConfig.kt` | `@EnableJpaAuditing` — required for `@CreatedDate`/`@LastModifiedDate` on `BaseOrmEntity` to populate |
| `src/main/kotlin/com/remine/common/domain/exception/DomainException.kt` | Abstract base for all domain-level exceptions |
| `src/main/kotlin/com/remine/common/domain/exception/EntityNotFoundException.kt` | → HTTP 404 |
| `src/main/kotlin/com/remine/common/domain/exception/InvalidRequestException.kt` | → HTTP 400 |
| `src/main/kotlin/com/remine/common/domain/exception/UnauthorizedException.kt` | → HTTP 401 |
| `src/main/kotlin/com/remine/common/domain/exception/ForbiddenException.kt` | → HTTP 403 |
| `src/main/kotlin/com/remine/common/web/ApiResponse.kt` | Standard response envelope `{data, error}` with `ok(data)`/`fail(code, message)` factories |
| `src/main/kotlin/com/remine/common/web/ApiError.kt` | `{code, message}` shape used inside `ApiResponse.error` |
| `src/main/kotlin/com/remine/common/web/GlobalExceptionHandler.kt` | `@RestControllerAdvice` mapping each domain exception (plus Spring Security's `AccessDeniedException` and a catch-all `Exception`) to an `ApiResponse.fail(...)` with the matching HTTP status |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/common/config` | Cross-cutting Spring `@Configuration` classes |
| `src/main/kotlin/com/remine/common/domain/exception` | Shared domain exception hierarchy |
| `src/main/kotlin/com/remine/common/persistence` | Base JPA entity |
| `src/main/kotlin/com/remine/common/web` | Shared HTTP response envelope + global exception handling |

## For AI Agents

### Working In This Directory
- **Every new JPA entity anywhere in the codebase must extend `BaseOrmEntity`** (constructor param `id: UUID = UUID.randomUUID()`), inheriting `createdAt`/`updatedAt` (auditing-managed, `protected set`) and `deletedAt` (soft delete via `softDelete()`). Never add a standalone `is_active` boolean for delete semantics — that is a documented anti-pattern (root `CLAUDE.md`) that previously caused ghost-data bugs.
- **`@Where(clause = "deleted_at IS NULL")` (or `@SQLRestriction` on newer Hibernate) does NOT propagate from `BaseOrmEntity` to subclasses** on this project's Hibernate version (5.6) — this is called out in a comment on `BaseOrmEntity.kt` itself. Every subclass entity (see `call`'s `CallLogJpaEntity` for the reference pattern) must re-declare that annotation itself, or soft-deleted rows will silently leak back into queries.
- **Throw the shared domain exceptions, don't invent new ones per-module**, unless a genuinely new HTTP-status category is needed — `GlobalExceptionHandler` already wires `EntityNotFoundException`→404, `InvalidRequestException`→400, `UnauthorizedException`→401, `ForbiddenException`→403. A new exception type added to another module will fall through to the generic 500 handler unless a matching `@ExceptionHandler` is also added here.
- **`GlobalExceptionHandler` distinguishes** the domain `ForbiddenException` (business-rule denial, e.g. "not a participant of this call") from Spring Security's `AccessDeniedException` (thrown by `@PreAuthorize` role checks) — both currently map to 403, but they are semantically different failure sources; keep using `@PreAuthorize` for role/permission checks and the domain exception for business-rule checks, per root `CLAUDE.md`'s "authorization via `@PreAuthorize` only" rule.
- Controllers across the codebase should return `ApiResponse<T>` (via `ApiResponse.ok(data)`) rather than raw DTOs/`ResponseEntity`, so success and error shapes stay consistent — see `CallController` for the pattern.
- `common`'s own `build.gradle.kts` deliberately keeps `jar { enabled = true }` and uses `api(...)` for its Spring Boot starters (not `implementation`) so downstream modules transitively get JPA/web on their classpath without redeclaring them.

### Testing Requirements
No test files exist under `src/test/kotlin` for this module yet — the classes here are thin enough (data holders, simple exception mapping) that they are currently exercised only indirectly through consuming modules' tests (e.g. `call`'s `CallServiceTest`/`CallControllerTest` exercise `EntityNotFoundException`/`ForbiddenException` end-to-end).

### Common Patterns
- Exceptions are minimal one-line subclasses of `DomainException(message: String)` — no extra fields, no error codes on the exception itself; the HTTP-facing `code` string (`"NOT_FOUND"`, `"FORBIDDEN"`, etc.) is assigned only in `GlobalExceptionHandler`, keeping domain code free of HTTP concerns.
- `BaseOrmEntity` uses `protected set` on auditing fields so subclasses can read but not directly assign `createdAt`/`updatedAt`/`deletedAt` outside of Spring Data's `AuditingEntityListener` and the explicit `softDelete()` method.

## Dependencies

### Internal
None — `common` is a leaf/shared-kernel module with no dependency on any other `Remine_backend` module.

### External
- `spring-boot-starter-data-jpa` (`api`) — `@MappedSuperclass`, JPA annotations, Spring Data auditing (`@EnableJpaAuditing`, `@CreatedDate`/`@LastModifiedDate`)
- `spring-boot-starter-web` (`api`) — `ResponseEntity`, `@RestControllerAdvice`/`@ExceptionHandler`
- `com.fasterxml.jackson.module:jackson-module-kotlin` (`implementation`) — Kotlin-friendly JSON serialization for `ApiResponse`/`ApiError`
- `org.springframework.security:spring-security-core` (`implementation`, core lib only — not the Boot starter) — needed solely so `GlobalExceptionHandler` can catch `AccessDeniedException` without pulling security auto-configuration into every consumer of `common`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
