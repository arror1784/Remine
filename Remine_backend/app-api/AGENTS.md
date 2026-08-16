<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# app-api

## Purpose
The Spring Boot API server `bootJar` entry point. Owns nothing domain-specific itself — it composes every domain module (`activity`, `auth`, `user`, `notification`, `message`, `call`, `memory`, `family`), wires cross-cutting infrastructure (CORS, Redis), and hosts the small number of REST endpoints that legitimately span more than one domain module (family/mypage summary aggregation), since no domain module is allowed to depend on another domain module directly.

## Key Files
| File | Description |
|------|-------------|
| `RemineApplication.kt` | `@SpringBootApplication` entry point; explicitly sets `scanBasePackages`/`@EnableJpaRepositories`/`@EntityScan` to `com.remine` because every module lives under its own `com.remine.<module>` package rather than a subtree of `com.remine.app` |
| `composition/FamilySummaryController.kt` | `GET /api/v1/family/summary` — aggregates `memory`'s `GetMemoryStatsQuery`, `call`'s `GetCallStatsQuery`, and `message`'s `GetChatThreadQuery` into one response |
| `composition/MyPageStatsController.kt` | `GET /api/v1/users/me/stats` — aggregates `user`'s `GetMyProfileQuery`, `memory`'s `GetMemoryStatsQuery`, and `activity`'s `GetWeeklyPatternQuery` |
| `config/CorsConfig.kt` | `WebMvcConfigurer` allowing `http://localhost:5173`/`5174` (Vite dev servers) on `/api/**` with credentials |
| `config/RedisConfig.kt` | `RedisTemplate<String, Any>` (string-serialized) bean and a `RedisLockRegistry` (`"remine-lock"`) for distributed locks |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `composition/` | Cross-domain aggregation REST controllers only — no domain logic, only calls into other modules' inbound query ports |
| `config/` | App-wide Spring configuration (CORS, Redis; security config itself lives in the `auth` module, not here) |

## For AI Agents

### Working In This Directory
- This module is the *only* place cross-domain aggregation endpoints are allowed to live, per an explicit comment in `FamilySummaryController.kt`/`MyPageStatsController.kt`: "no domain module is allowed to depend on another." If a new feature needs data from two-plus domain modules in one response, add a controller here rather than adding a cross-module dependency to a domain module's `build.gradle.kts`.
- Every composition controller resolves the acting parent user via `RemineUserPrincipal.parentUserId()` from `auth`, matching the convention in domain modules (e.g. `activity`) — a CHILD-role caller's requests resolve against their paired parent's data.
- The Redis Stream-based task queue mentioned in root CLAUDE.md (shared with a Python/AI service) is **not yet wired up** — `RedisConfig.kt` has a comment flagging this as a TODO once that integration exists; don't assume a `StreamMessageListenerContainer` bean exists.
- Security's `SecurityFilterChain` (authenticated-vs-not) is defined in `auth`'s `SecurityConfig`, not here; this module only adds CORS at the MVC level via `CorsConfig`. Note the ordering pitfall documented in `SecurityConfig`: Spring Security's filter chain runs before MVC-level CORS, so `OPTIONS` must be explicitly `permitAll()`'d in `auth` or preflight requests get rejected before CORS headers are attached.
- `application.yml` / `application-local.yml` / `application-prod.yml` live under `src/main/resources/`; prod config expects `ddl-auto=validate` per root CLAUDE.md (Flyway migrations, not entity-driven schema changes) — check these files before assuming defaults.
- The `src/main/resources/.omc/` subtree present in this module is unrelated OMC agent-runtime state, not application resource content — ignore it when reasoning about this module's actual Spring resources.

### Testing Requirements
No tests yet under `src/test/kotlin` for this module.

### Common Patterns
- Composition controllers are plain `@RestController`s (no `@RequestMapping` class-level prefix) with one `@GetMapping` method each, calling 2-3 other modules' `*Query` inbound ports directly and assembling the results into a locally-defined response `data class` — they do not implement any Command/Query port themselves and have no `application`/`domain` layers of their own.

## Dependencies

### Internal
`common`, `client-openai`, `migration`, `auth`, `user`, `notification`, `message`, `call`, `memory`, `activity`, `family`

### External
- `spring-boot-starter-web` — REST/MVC
- `spring-boot-starter-data-jpa` — JPA bootstrap for all composed modules' entities
- `spring-boot-starter-data-redis` + `spring-integration-redis` — Redis template/lock registry
- `spring-boot-starter-actuator` — health/ops endpoints (`/actuator/health` is public per `auth`'s `SecurityConfig`)
- `flyway-core` — schema migrations
- `springdoc-openapi-ui:1.7.0` — Swagger UI (`/swagger-ui.html`, `/v3/api-docs/**`, public per `SecurityConfig`)
- `com.h2database:h2` (runtime) — dev DB
- `org.postgresql:postgresql` (runtime) — prod DB

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
