# Overnight Mission Report

Started: 2026-08-17 (KST), after a ~25min delay to land on a fresh session token window.
Mode: fully autonomous — no commit/push/approval confirmations during this run, per explicit user instruction.
Execution order: sequential (not parallel), to avoid concurrent git/build races in the same working tree.

## Status: 🟡 In progress — Task 1 ✅ done, Task 2 ✅ done, Task 3 ✅ done, Task 4 starting

## Task 1 — Backend test coverage
- Scope: `app-api`, `auth`, `user`, `common`, `client-openai`, `migration` (originally briefed as zero-test modules), following the existing hand-rolled-fake pattern used in `memory`/`family`/`message`/`call`/`notification`/`activity`.
- Status: ✅ Done
- Summary:
  - **Scope correction**: on inspection, `user` already had 3 committed test files (`UserServiceTest`, `UserControllerTest`, `DemoLoginServiceTest`, from the initial scaffold commit `1cd572e`) — the task brief was stale on this point. Actual work covered the remaining 5 modules that were genuinely at zero: `common`, `auth`, `client-openai`, `app-api`, `migration`.
  - **`common`** (12 tests, 3 files): `BaseOrmEntityTest` (soft-delete transitions `isDeleted`/`deletedAt`, id-based `equals`/`hashCode`), `ApiResponseTest` (`ok`/`fail` envelope construction), `GlobalExceptionHandlerTest` (all 6 handler methods — `EntityNotFoundException`→404, `InvalidRequestException`→400, `UnauthorizedException`→401, `ForbiddenException`→403, Spring Security's `AccessDeniedException`→403, catch-all `Exception`→500 with a generic message).
  - **`auth`** (13 tests, 3 files): `JwtTokenProviderTest` (issue/parse round-trip for paired child and unpaired parent, and null-return on garbage token / wrong signing secret / expired token), `RemineUserPrincipalTest` (`parentUserId()` for PARENT vs. paired/unpaired CHILD including the `InvalidRequestException` throw, `counterpartUserId()` for both roles), `JwtAuthenticationFilterTest` (missing header, invalid token, and valid token → `SecurityContextHolder` populated with the right principal + `ROLE_*` authority; used Spring's `MockHttpServletRequest`/`MockHttpServletResponse` plus a hand-rolled `FilterChain` fake, calling the filter's public `doFilter` entry point since `doFilterInternal` stays protected across the override).
  - **`client-openai`** (4 tests, 1 file): `OpenAiClientTest` covering blank-API-key guard, a 2xx success response, a non-2xx response, and a response missing message content — all mapped to `OpenAiClientException`. Required one small production-code change: `OpenAiClient`'s previously hardcoded `private val restTemplate = RestTemplate()` became a constructor parameter defaulting to `RestTemplate()`, so tests can bind Spring's `MockRestServiceServer` to a controlled instance instead of hitting the real OpenAI API. Verified this is a safe, behavior-preserving change — the two existing subclasses in `memory`/`activity` (`StubOpenAiClient` in `OpenAiMemoryQuizGeneratorTest`/`OpenAiActivityRecommendationGeneratorTest`) construct via named `apiKey`/`model` args and are unaffected by the new trailing default param.
  - **`app-api`** (5 tests, 2 files): favored high-value tests over exhaustive/context-booting ones per the task's own ROI guidance. `FamilySummaryControllerTest` and `MyPageStatsControllerTest` unit-test the two cross-domain composition controllers with fake query ports, covering paired-parent aggregation, the unpaired-principal zero-message-count path, and CHILD-principal requests resolving against the paired parent's data. Deliberately did **not** add a `@SpringBootTest` context-loads test: there is zero precedent for `@SpringBootTest` anywhere in this codebase (every one of the 6 pre-existing well-tested modules uses pure unit tests with hand-rolled fakes, no Spring context), and `app-api`'s dev datasource (`application.yml`) points at a file-backed H2 DB (`./data/remine`, `AUTO_SERVER=TRUE`) that the potentially-running local dev server on :8080 may hold open — booting a real context in the test JVM risked either a file-lock conflict or, if pointed at an in-memory H2 profile instead, introducing a first-of-its-kind test-only Spring profile with no established convention to follow. Judgment call: skip it and note here rather than invent a new pattern unsupervised.
  - **`migration`**: no code added, per the task's own guidance — it's a resource-only module (11 Flyway SQL files, no Kotlin source, no `src/test`), and its correctness is implicitly exercised every time any other module's Spring context boots (Flyway applies/validates migrations at startup). Confirmed no `src/test` directory exists and none was warranted.
  - **Delegation**: none — all of this was judgment-heavy (exception-mapping correctness, JWT/security logic, HTTP-client test-double wiring, cross-domain aggregation semantics) per the task's own guidance to keep such work direct rather than delegating to Antigravity.
  - **Verification**: `:common:test`, `:auth:test`, `:client-openai:test`, `:app-api:test` each run and pass individually, and a final full `./gradlew test` across all 12 modules is `BUILD SUCCESSFUL` (58 actionable tasks, 16 executed + 42 up-to-date on the last run) with zero regressions in the 6 previously-existing test suites.
  - **Total new tests added**: 34 (12 + 13 + 4 + 5), across 9 new test files, plus 1 minor DI refactor in `OpenAiClient.kt`.

## Task 2 — Full code review + safe cleanup
- Scope: whole repo (backend + frontend), simplification/dedup/consistency fixes only where safe and verifiable.
- Status: ✅ Done
- Summary:

### What was reviewed
Read every area's `AGENTS.md` (root, `Remine_backend/`, `Remine_frontend/`) plus root `CLAUDE.md`, then used the standing conventions as an explicit review checklist across all 292 backend Kotlin files and 58 frontend TS/TSX files. Convention audits run mechanically rather than by spot-check:

| Check | Result |
|---|---|
| Every JPA entity extends `BaseOrmEntity` **and** redeclares `@Where(deleted_at IS NULL)` | ✅ all 17 entities compliant |
| Domain layer free of Spring/JPA/Jackson imports | ✅ zero violations |
| No domain module depends on another domain module | ✅ only `message`→`notification`, which `Remine_backend/AGENTS.md` documents as intentional |
| No `PUT` mappings (create=POST, update=PATCH, delete=DELETE, read=GET) | ✅ zero `@PutMapping` |
| No hardcoded hex / `text-[#...]` outside `src/theme.ts` | ✅ zero |
| Components never call `axios`/`fetch` directly | ✅ zero — all traffic goes through `src/api/<domain>.ts` |
| Every JPA `@Column` backed by a Flyway migration (prod+dev both run `ddl-auto=validate`) | ✅ zero drift |
| Frontend→backend contract drift | ✅ all 30 frontend calls resolve to one of the 44 real endpoints |
| Leftover `TODO`/`FIXME`/`HACK`, `console.log`, `@Disabled`/`.skip`/`.only` | ✅ zero remaining |
| Unused frontend exports / unreferenced Kotlin classes | ✅ zero |

Note on method: the contract-drift check initially reported a clean result **vacuously** — the extraction regex broke on nested generics (`http.get<ApiEnvelope<NotificationItem[]>>`), matching zero calls. Caught it by asserting on the match count, fixed the regex, and re-ran to get the real 30/30 result above. Flagging because a green check that ran against an empty set is the failure mode most likely to hide real drift from a later reader.

### Changed (3 commits, each verified before the next)
1. **Removed the stale `UserController` signup TODO** — it claimed `SecurityConfig` still needed a `permitAll` entry for `/api/v1/users/signup`; `SecurityConfig.kt:45` already has exactly that rule. Comment-only, no behavior change.
2. **Fixed both `oxlint` `exhaustive-deps` warnings** (`pages/{parent,child}/Home.tsx`) — confirmed the pre-existing suspicion was right: `refreshUnreadCount` is a Zustand action created once in the store initializer and never re-`set`, so it is referentially stable and adding it to the dep array cannot cause a re-run. Fixed cleanly rather than suppressed; **frontend lint is now at zero warnings**.
3. **Consolidated duplicated counterpart resolution** — `CallController` and `MessageController` each carried a byte-identical role-`when` block resolving "the other side of the pair", down to the same exception type and message. `RemineUserPrincipal` already hosts this exact shape (`parentUserId()` throws the mirror-image error), so it became `requireCounterpartUserId()` there. Branching, exception types and messages are unchanged; added 2 unit tests covering both roles paired and unpaired. Also dropped the 4 imports this made dead and one unused test binding in `GenerateMemoryQuizQuestionsServiceTest` (the assertions below it read from the repository, not the binding — this was the only Kotlin compiler warning in the build).
4. **Hoisted `ApiEnvelope<T>`/`unwrap<T>` into `src/api/http.ts`** — all 8 domain API clients held md5-identical private copies. `http.ts` already owns shared HTTP concerns, so it was the existing home, not a new abstraction. Net −61 lines. `family.ts`'s `getPairedProfile` still deliberately bypasses `unwrap` (a null `data` is a valid "not yet paired" answer) and its body is untouched.

### Found but deliberately NOT changed
- **`FamilySummaryController` counts messages with a magic cap.** `getChatThreadQuery(... limit = 1000).items.size` silently under-reports once a pair exceeds 1000 messages, and pulls 1000 rows to compute one integer. This is a real correctness bug, but fixing it properly needs a new `countByPair` query port in `message` — that is new API surface and a judgment call about the port contract, not a safe mechanical edit. Flagged for a follow-up rather than changed unsupervised.
- **`/parent/notifications`, `/child/notifications` and `/switch-mode` are registered only in `App.tsx`'s overlay `<Routes>`**, never the main tree. In normal flow they are always entered via `<Link state={{backgroundLocation}}>` so this works, but a direct URL or a hard refresh on those paths renders a blank phone frame. Whether they should also exist as standalone full pages is a product decision, so it is logged rather than guessed at.
- **Three parent/child page pairs are near-duplicates**: `Message` (10 differing lines / 193), `Call` (8/60), `Notifications` (8/103) — differing only by role prefix and accent color. The other four pairs genuinely diverge (`Home` 273/235, `Family` 309/338, `Today` 194/154, `MyPage` 194/201). Collapsing the three would remove ~350 duplicated lines, but role-parallel pages are the established structure of this app and doing so introduces a role-parameterized abstraction — explicitly out of scope for a safe-cleanup pass.
- **14 backend endpoints have no frontend caller** (most of the `activity` checklist/timeline/sync surface, `PATCH /users/me`, `GET /calls/stats`, `GET /memories/stats`). These look like a deliberately built-ahead API for in-flight features, not dead code, and `GET /calls/stats` + `GET /memories/stats` have their underlying query ports consumed server-side by `FamilySummaryController`. Deleting any of them would be a behavior change well outside this task.
- **`CompleteMemoryQuizWithAnswersService.kt:42`'s `!!`** is the only non-null assertion in the backend and is guarded by a `containsKey` check in the same `when` condition — correct as written, so left alone.
- **Only one `@PreAuthorize` exists in the entire codebase** (`UserController`'s CHILD-only pairing endpoint). The design leans on server-side ownership resolution from the principal instead of role gates, which is defensible, but the balance of the two is a **security-review question — handed to Task 3, not assessed here.**

### Verification evidence
- Backend: full `./gradlew test` green after every commit — final run `BUILD SUCCESSFUL`, **154 tests, 0 failures/errors, 0 skipped** (counted from the JUnit XML, not from Gradle's up-to-date summary, since an up-to-date task prints success without running anything).
- Frontend: `yarn build` (`tsc -b && vite build`) succeeds and `yarn lint` reports **zero warnings**, down from the 2 pre-existing ones.
- Entity/migration alignment verified statically because no test boots a Spring context, so `ddl-auto=validate` drift would otherwise only surface at startup.

### Delegated to Antigravity
The `ApiEnvelope`/`unwrap` consolidation (item 4) — 8 mechanical, identical edits, the intended fit for delegation. `omc ask antigravity` edited the files directly and self-reported a clean `tsc -b` + `oxlint`. **Its core change was correct but its self-report was incomplete**: it left a stray 3-blank-line gap in all 8 files where each deleted `unwrap` had been, which neither `tsc` nor `oxlint` flags. Reviewed the diff rather than trusting the summary, collapsed the gaps, and re-verified build + lint myself. Everything else in this task was judgment-heavy (architectural intent, exception semantics, security posture) and kept as direct work.

## Task 3 — Security review pass
- Scope: OWASP Top 10 focus — auth/JWT, input validation, secrets handling, CORS, dependency issues.
- Status: ✅ done
- Summary: Reviewed both sides end-to-end. Found and fixed one real authorization-boundary bug (an IDOR on call start) and one real deployment-config risk (a prod JWT secret that could silently fall back to a secret committed in this repo), plus three lower-severity input-bounds gaps. Everything else checked out clean — notably the pair-scoping across `memory`/`family`/`notification`/`message` is genuinely enforced, and no secret was ever committed.

### Findings

| # | Severity | Area | Description | Disposition |
|---|----------|------|-------------|-------------|
| 1 | **High** | Auth / config | `application.yml` defaults `jwt.secret` to `dev-only-secret-key-change-in-production-…` so local dev boots unconfigured. `application-prod.yml` never overrode it, so a prod deploy with `JWT_SECRET` unset would boot successfully and sign tokens with a secret published in this repo — anyone could forge a JWT for any `userId`/`role`/`pairedUserId` and take over every account. | **Fixed** — prod declares `jwt.secret: ${JWT_SECRET}` with no default, so a missing env var fails the boot instead of silently degrading. |
| 2 | **Medium** | IDOR / authz | `POST /api/v1/calls` accepted a client-supplied `calleeId` that no layer validated. `CallLogJpaRepository.findHistoryByUserId` matches `callerId = :userId OR calleeId = :userId`, so a call log lands in *both* participants' history and stats — any authenticated user could plant fabricated call records in a stranger's account by posting that user's UUID. The frontend never sends the field (`call.ts` posts `{}`), so it was pure unvalidated attack surface. | **Fixed** — `StartCallService` now throws `ForbiddenException` when `calleeId != counterpartUserId`, mirroring the participant check `EndCallService` already had. Regression test added. |
| 3 | Low | Input validation | `UpdateProfileRequest` was `@Valid`-annotated but carried no constraints at all, and `SignUpRequest` had `@NotBlank` but no `@Size`. `app_user` bounds these columns (`name` 50, `age_group` 10, `interests` 255), so oversized input became a `DataIntegrityViolationException` → generic 500 rather than a clean 400. | **Fixed** — `@Size` added on both, matched to the column widths, including per-element bounds on the comma-joined `interests` list. |
| 4 | Low | Resource exhaustion | `limit` on `GET /messages/thread`, `GET /family/posts`, and `GET /calls` was an unbounded `Int` straight from the query string. | **Fixed** — coerced to `1..100` at the controller boundary. The frontend already requests 20/50, and `FamilySummaryController`'s internal `limit = 1000` goes through the query port directly, so neither is affected. |
| 5 | **High if ever deployed** | Auth design | `POST /api/v1/auth/demo-login` is `permitAll` and issues a real 7-day JWT for a fixed seeded pair with **no credentials at all**. In any real deployment this is an unauthenticated token-issuance endpoint — a complete auth bypass. | **Documented** — it is explicitly intentional (see the class doc on `AuthController`) and the entire demo flow depends on it. Recommended fix when this stops being a demo: `@Profile("!prod")` on `AuthController`. Not applied autonomously because it would silently break a prod-profile demo deploy. |
| 6 | Low | Frontend | JWTs are persisted to `localStorage` via Zustand `persist` (key `remine-auth`), so any XSS would exfiltrate a 7-day token. | **Documented** — known/accepted tradeoff for JWT-in-localStorage apps; a real fix means httpOnly cookies plus backend session changes. Mitigating factor: the XSS surface is currently empty (zero `dangerouslySetInnerHTML`, `eval`, `innerHTML`, or `new Function` anywhere in `src/`). |
| 7 | Low | Dependencies | Spring Boot 2.7.18 is past its OSS support window (`javax.*` era); `jjwt` is 0.11.5 vs. current 0.12.x. | **Documented** — no known-exploitable CVE in the way they're used here, and a 3.x upgrade is a whole-repo `javax`→`jakarta` migration. Out of scope for a safe autonomous pass. |
| 8 | Informational | Security headers | `SecurityConfig` disables `frameOptions()` globally (needed for the H2 console) and `permitAll`s `/h2-console/**` unconditionally. | **Documented** — `application-prod.yml` sets `spring.h2.console.enabled: false`, so the servlet isn't registered in prod and the rule 404s. Clickjacking risk on a pure-JSON API is negligible. Worth profile-gating if a browser-rendered page is ever served from this app. |

### Verified clean (no action needed)
- **Pair-scoping / IDOR elsewhere** — spot-checked `memory`, `family`, `notification`, and `message`. Every one resolves scope from the principal, never from the request: `memory` passes `principal.parentUserId()` and each service calls `requireOwnedByCaller`; `family` passes a principal-derived `pairUserIds` set and `FamilyPostService.requireOwnPair` enforces it on like/reply/read; `notification` looks up via `findByIdAndRecipientUserId` so another user's notification id returns 404, not someone else's row; `message` derives both thread participants from `requireCounterpartUserId()`. The call-start bug in finding 2 was the only place a request-supplied ID was trusted.
- **JWT algorithm** — `parseClaimsJws` with a `SecretKey` set. `alg: none` is rejected (unsigned JWTs fail `parseClaimsJws`), and no RS256/HS256 confusion is reachable since only MAC algorithms match a `SecretKey`. Signature, expiry, and wrong-secret rejection all have passing tests.
- **SQL injection** — zero native queries, zero `JdbcTemplate`, zero string-concatenated JPQL. All 8 `@Query` uses are parameterized JPQL with `@Param` binding.
- **Secrets** — no real secret in the working tree (every grep hit is a test placeholder like `"test-key"`/`"stub-key"`) and none in git history across all 50 commits — every historical config hit is an env-var placeholder (`${H2_DB_PASSWORD:}`, `${OPENAI_API_KEY:}`, `${GOOGLE_OAUTH_CLIENT_SECRET:}`). `application-local.yml` (which does hold a real local H2 password) is correctly untracked and ignored via `Remine_backend/.gitignore:17`; only `application-local.yml.example` is committed, carrying the placeholder `your-h2-console-password-here`. The OpenAI key is `@Value`-injected and only ever passed to `setBearerAuth`, never logged.
- **CORS** — `CorsConfig` allows exactly two explicit localhost origins with `allowCredentials(true)`. That combination is only invalid/dangerous with a `*` origin, which is not used here. Prod will need its real origin added.
- **Error handling** — `GlobalExceptionHandler`'s catch-all logs the exception server-side and returns a generic `"Internal server error"`, so no stack traces or internals reach the client.
- **Frontend dependencies** — `yarn audit`: 0 vulnerabilities across 176 packages.
- **CSRF** — correctly disabled; auth is a stateless `Authorization` header, not a cookie, so there is nothing for CSRF to ride on.

### Changed
- `Remine_backend/app-api/src/main/resources/application-prod.yml` — `jwt.secret` with no default.
- `Remine_backend/call/.../StartCallCommand.kt`, `StartCallService.kt`, `CallController.kt` — counterpart check on call start.
- `Remine_backend/call/src/test/.../CallServiceTest.kt` — regression test `start call throws ForbiddenException when calleeId is not the caller's counterpart`, plus existing cases updated for the new `In` signature.
- `Remine_backend/user/.../SignUpRequest.kt`, `UpdateProfileRequest.kt` — `@Size` bounds matched to schema.
- `Remine_backend/message/.../MessageController.kt`, `family/.../FamilyPostController.kt`, `call/.../CallController.kt` — `limit` coerced to `1..100`.

### Verification
`JAVA_HOME=/opt/homebrew/opt/openjdk@17 ./gradlew test` → **BUILD SUCCESSFUL, 155 tests, 0 failures** (up from 154; `CallServiceTest` 6→7). Confirmed the new test actually executed by grepping its name out of `call/build/test-results/`. `yarn audit` → 0 vulnerabilities. Single commit `6ba2007`, verified green before commit and pushed to `origin/main`.

### Delegated
Nothing. Antigravity was available for mechanical scanning, but every step here was either a judgment call (exploitability, severity, whether a fix was safe to apply unattended) or a grep small enough that delegating would have cost more than it saved.

## Task 4 — E2E test infrastructure
- Scope: Playwright (or equivalent) installed as a real, re-runnable committed test suite covering core flows (demo login, parent Today/Gallery, child Family/Message).
- Status: not started
- Summary: _pending_

## Blockers / decisions log
_pending_

## Commits made this run
_pending_
