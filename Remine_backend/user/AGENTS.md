<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# user

## Purpose
The `user` module owns the `User` account aggregate: signup, profile, and the parent↔child pairing relationship that underlies the whole product (a parent's `pairedUserId` points at their child and vice versa). It also owns two auth-adjacent entry points that don't fit the `auth` module — `/api/v1/auth/demo-login` (a credential-free login into one of two fixed, pre-seeded demo accounts, for zero-friction demos/reviews) and `/api/v1/users/me/pairing` (a child redeeming a parent's invite code). Both live here rather than in `auth` because `auth` cannot depend on `user` (dependencies point inward/one-way: `user` already depends on `auth`, not the reverse). Only `PARENT` users get an `inviteCode` and `interests`; `CHILD` users get neither.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/user/domain/User.kt` | Domain entity (id, role, name, ageGroup, interests, email, googleId, inviteCode, pairedUserId, streakDays); `updateProfile()` only applies `interests` changes for `Role.PARENT`; `pairWith(targetUserId)` sets `pairedUserId` |
| `src/main/kotlin/com/remine/user/application/port/inbound/SignUpCommand.kt` | Command port: creates a user, returns userId/inviteCode/accessToken |
| `src/main/kotlin/com/remine/user/application/port/inbound/UpdateProfileCommand.kt` | Command port: patches name/ageGroup/interests |
| `src/main/kotlin/com/remine/user/application/port/inbound/PairWithInviteCodeCommand.kt` | Command port: child redeems a parent's invite code |
| `src/main/kotlin/com/remine/user/application/port/inbound/DemoLoginCommand.kt` | Command port: role-only login into the fixed demo parent/child accounts |
| `src/main/kotlin/com/remine/user/application/port/inbound/GetMyProfileQuery.kt` | Query port: fetch the caller's own profile |
| `src/main/kotlin/com/remine/user/application/port/inbound/GetPairedUserQuery.kt` | Query port: fetch the caller's paired counterpart, if any |
| `src/main/kotlin/com/remine/user/application/port/outbound/UserRepositoryPort.kt` | Outbound port: save, findById, findByInviteCode, existsByInviteCode, findByGoogleId |
| `src/main/kotlin/com/remine/user/application/service/SignUpService.kt` | Validates `ageGroup` against a fixed set, generates a unique `REMIND-XXXX` invite code for parents, issues a JWT |
| `src/main/kotlin/com/remine/user/application/service/UpdateProfileService.kt` | Validates `ageGroup` (reuses `SignUpService.VALID_AGE_GROUPS`), applies `User.updateProfile()` |
| `src/main/kotlin/com/remine/user/application/service/PairWithInviteCodeService.kt` | Looks up parent by invite code, links both users' `pairedUserId`, re-issues the child's JWT with the new `pairedUserId` claim |
| `src/main/kotlin/com/remine/user/application/service/DemoLoginService.kt` | Looks up one of two hardcoded demo UUIDs by role, issues a JWT; UUIDs must stay in sync with `migration`'s `V8__seed_demo_users.sql` |
| `src/main/kotlin/com/remine/user/application/service/GetMyProfileService.kt` | Implements `GetMyProfileQuery` |
| `src/main/kotlin/com/remine/user/application/service/GetPairedUserService.kt` | Implements `GetPairedUserQuery`; returns `null` (not an error) when unpaired |
| `src/main/kotlin/com/remine/user/adapter/presentation/web/UserController.kt` | REST controller at `/api/v1/users` |
| `src/main/kotlin/com/remine/user/adapter/presentation/web/AuthController.kt` | REST controller at `/api/v1/auth` — demo-login only |
| `src/main/kotlin/com/remine/user/adapter/infrastructure/jpa/UserJpaEntity.kt` | JPA entity for table `app_user`; stores `interests` as a comma-joined `VARCHAR`, soft-delete via `@Where` |
| `src/main/kotlin/com/remine/user/adapter/infrastructure/jpa/UserJpaRepository.kt` | Spring Data repository: findByInviteCode, existsByInviteCode, findByGoogleId |
| `src/main/kotlin/com/remine/user/adapter/infrastructure/jpa/UserRepositoryAdapter.kt` | Implements `UserRepositoryPort` |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/user/domain/` | Domain entity, no Spring/JPA dependencies |
| `src/main/kotlin/com/remine/user/application/port/inbound/` | CQRS Command/Query interfaces with In/Out inner classes |
| `src/main/kotlin/com/remine/user/application/port/outbound/` | Outbound repository port interface |
| `src/main/kotlin/com/remine/user/application/service/` | Service implementations of the inbound ports |
| `src/main/kotlin/com/remine/user/adapter/presentation/web/` | REST controllers and request/response DTOs |
| `src/main/kotlin/com/remine/user/adapter/infrastructure/jpa/` | JPA entity, Spring Data repository, and the outbound port adapter |

## For AI Agents

### Working In This Directory
- **Endpoints** (all under `/api/v1/users` unless noted):
  - `POST /signup` — public; currently has a `TODO` comment in `UserController` flagging that `auth` module's `SecurityConfig.kt` permitAll list needs `/api/v1/users/signup` added as a follow-up — check that config is actually wired before assuming signup works end-to-end unauthenticated.
  - `PATCH /me` — authenticated, updates the caller's own profile.
  - `POST /me/pairing` — authenticated, `@PreAuthorize("hasRole('CHILD')")` only (a parent cannot call this to pair itself).
  - `GET /me` — authenticated, caller's own profile.
  - `GET /me/paired` — authenticated, caller's paired counterpart or `null`.
  - `POST /api/v1/auth/demo-login` — public, in `AuthController`, not `UserController`.
- `SignUpService.VALID_AGE_GROUPS` is the single source of truth for valid age groups (`"10대"` … `"80대"`, `"기타"`) and is reused by `UpdateProfileService` — don't duplicate this set elsewhere.
- The two demo account UUIDs (`DemoLoginService.DEMO_PARENT_ID` / `DEMO_CHILD_ID`) are hardcoded and must match `migration/src/main/resources/db/migration/V8__seed_demo_users.sql` exactly — if you add/change demo accounts, update both places together.
- `interests` is persisted as a single comma-joined `VARCHAR(255)` column on `app_user`, not a separate table — parsing/joining happens in `UserJpaEntity.toDomain()`/`fromDomain()`. Only `PARENT` role ever has non-empty `interests`; both `SignUpService` and `User.updateProfile()` enforce this by silently dropping interests for non-parents rather than erroring.
- Pairing is bidirectional and written in one transaction (`PairWithInviteCodeService`): both the parent and child rows get their `pairedUserId` set and saved before a new JWT is issued to the child (the JWT embeds `pairedUserId` as a claim, so it must be re-issued after pairing).

### Testing Requirements
`src/test/kotlin/com/remine/user/` covers:
- `adapter/presentation/web/UserControllerTest.kt` — signup response mapping, that `pairWithInviteCode` carries the `@PreAuthorize("hasRole('CHILD')")` annotation (reflection-based check, not a full security-filter integration test), and a successful pairing call through the controller.
- `application/service/DemoLoginServiceTest.kt` — issuing a token for the seeded parent account, and throwing `EntityNotFoundException` when the demo seed hasn't been applied.
- `application/service/UserServiceTest.kt` — the broadest test file: parent signup (invite code + interests kept), child signup (no invite code, interests dropped), invalid age group rejection, profile update, full pairing flow (both sides linked), invalid invite code rejection, and both query services (`getMyProfile`, `getPairedUser` before/after pairing).

All service tests use an in-memory fake `UserRepositoryPort` (a `ConcurrentHashMap`-backed `FakeUserRepository` in `UserServiceTest`, a simple `Map`-backed `StubUserRepository` in `DemoLoginServiceTest`) plus a real `JwtTokenProvider` constructed with a test secret — no mocking framework, no Spring context.

### Common Patterns
- CQRS: every inbound port is a `fun handle(command/query: In): Out` interface with `In`/`Out` as nested `data class`es, one interface per file — identical shape to `notification`.
- Services are `@Service @Transactional` (write) or `@Service @Transactional(readOnly = true)` (read).
- Not-found cases throw `EntityNotFoundException`; business-rule violations (invalid age group, pairing with self) throw `InvalidRequestException` — both from `common`.
- Services that issue tokens depend directly on `auth`'s `JwtTokenProvider` rather than there being a token-issuing port in this module.

## Dependencies

### Internal
- `common` (`api(project(":common"))`) — base entity, `ApiResponse`, shared exceptions
- `auth` (`implementation(project(":auth"))`) — `Role`, `RemineUserPrincipal`, `JwtTokenProvider`

### External
- `spring-boot-starter-data-jpa` — JPA entity + Spring Data repository
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-validation` — `@Valid` request DTOs (`SignUpRequest`, `PairingRequest`, `DemoLoginRequest`)
- `spring-boot-starter-security` — `@PreAuthorize`, `@AuthenticationPrincipal`
- `h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
