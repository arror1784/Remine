# Overnight Mission Report

Started: 2026-08-17 (KST), after a ~25min delay to land on a fresh session token window.
Mode: fully autonomous — no commit/push/approval confirmations during this run, per explicit user instruction.
Execution order: sequential (not parallel), to avoid concurrent git/build races in the same working tree.

## Status: ✅ Mission complete — all 4 tasks done

## Final Summary

All four tasks completed and pushed to `origin/main` across **14 commits**, with the working tree clean and every verification gate green at the end: backend `./gradlew test` **155 tests, 0 failures**; frontend `yarn build` (which is the type check) succeeding; `yarn lint` at **zero warnings**; and a new `yarn test:e2e` suite at **11/11 passing**.

### 👉 What to look at first

**Read the Task 3 findings table.** It contains the only thing in this report that is urgent: a **High-severity JWT secret fallback** — `application-prod.yml` never overrode the dev default, so a production deploy with `JWT_SECRET` unset would have booted happily and signed tokens with a secret published in this repo, letting anyone forge a JWT for any user. That is now fixed (prod fails to boot without the env var), but it means **`JWT_SECRET` must be set in the deploy environment before the next prod deploy or the app will refuse to start** — an intentional, correct failure, but a surprising one if you haven't read this. The same table has a Medium IDOR on call start (also fixed, with a regression test) and finding #5, the unauthenticated `demo-login` endpoint, which is fine today and a complete auth bypass the day this stops being a demo.

After that: the app is running right now on the ports it was left on, and `cd Remine_frontend && yarn test:e2e` gives you a 20-second live demonstration that the five core flows actually work end to end.

### Outcome per task
| Task | Outcome |
|---|---|
| 1 — Backend test coverage | **34 new tests, 9 files**, covering the 5 genuinely untested modules (`common`, `auth`, `client-openai`, `app-api`; `migration` correctly needed none). The brief was stale on `user`, which already had 3 test files. One small DI refactor in `OpenAiClient` to make it testable without hitting the real API. |
| 2 — Code review + cleanup | Whole repo audited against its own conventions **mechanically, not by spot-check** — all 10 checks clean across 292 Kotlin and 58 TS/TSX files. Four safe cleanups applied (stale TODO, 2 lint warnings → zero, duplicated counterpart resolution, duplicated `ApiEnvelope`/`unwrap` at −61 lines). Six real issues found and deliberately *not* changed, each with a reason. |
| 3 — Security review | **2 real vulnerabilities found and fixed** (High: prod JWT secret fallback; Medium: IDOR letting any user plant call records in a stranger's history), plus 3 low-severity input-bounds fixes and a regression test. 5 further items documented with rationale. Verified clean on pair-scoping, SQL injection, secrets in history, CORS, CSRF, and error leakage. |
| 4 — E2E infrastructure | **11 Playwright tests, 5 specs, all passing** against the live backend, committed and re-runnable. Assertions target real API data rather than mere visibility, because several screens render static fallbacks through identical markup. The broken-image assertion was validated with a negative control. |

### Recommended follow-ups
Roughly in priority order — none are blocking, and none were applied autonomously because each is a behavior or product decision rather than a safe mechanical edit:

1. **Set `JWT_SECRET` in the prod environment** before the next deploy (see above).
2. **Gate `demo-login` behind `@Profile("!prod")`** the moment this stops being a demo build (Task 3, finding 5).
3. **Map `OpenAiClientException` in `GlobalExceptionHandler`** — `GET /activities/recommendation` currently 500s whenever the OpenAI key is absent or the provider is down (Task 4, finding 1). The frontend hides it, which is exactly why it's easy to miss.
4. **Add a `countByPair` query port to `message`** and drop `FamilySummaryController`'s `limit = 1000` magic cap, which both under-reports past 1000 messages and pulls 1000 rows to compute one integer (Task 2).
5. **Decide whether `/parent/notifications`, `/child/notifications` and `/switch-mode` should exist as standalone pages** — today a direct URL or hard refresh on those paths renders a blank phone frame (Task 2).
6. **Plan the Spring Boot 2.7 → 3.x upgrade.** 2.7.18 is past its OSS support window; it's a whole-repo `javax`→`jakarta` migration, so it needs scheduling rather than opportunism (Task 3, finding 7).
7. **Extend the E2E suite** as features land — the harness, typed API client and testid conventions are in place, so a new flow is now a short spec rather than new infrastructure. `Remine_frontend/AGENTS.md` documents how to run and extend it.

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
- Status: ✅ Done
- Summary: **11 Playwright tests across 5 specs, all passing headless against the live backend**, run with `yarn test:e2e`. Committed and re-runnable, not a one-off browser-driving session.

### What was installed
`@playwright/test` 1.62.1 (+ `playwright`/`playwright-core`) as devDependencies via Yarn. The Chromium binary was already present in `~/Library/Caches/ms-playwright/` from an earlier install, so `yarn playwright install chromium` returned instantly — **verified rather than assumed**, by resolving `chromium.executablePath()` and stat-ing the file (`chromium-1234/.../Google Chrome for Testing`, exists: true). New scripts: `test:e2e`, `test:e2e:ui`, `test:e2e:install`.

### Harness design
| Piece | Decision |
|---|---|
| `playwright.config.ts` | `baseURL` `http://localhost:5173` (override via `E2E_BASE_URL`), phone-sized 430×932 viewport, `workers: 1`, trace/screenshot retained on failure. |
| `webServer` | `reuseExistingServer: true` — reuses the dev server if one is up, spawns `yarn dev --port <n> --strictPort` if not. Chose this over "assume servers are up" so the suite is runnable from a cold checkout, and over always-spawning so it doesn't fight the running dev server for the port. |
| `e2e/global-setup.ts` | Pings `/actuator/health` and throws with the exact `gradlew :app-api:bootRun` command if the backend is down. Deliberately does **not** try to boot Gradle itself — that needs `JAVA_HOME` juggling and ~40s, and a suite that silently boots infrastructure is harder to debug than one that tells you what's missing. |
| `e2e/helpers/backend.ts` | Typed API client used by the specs, which **type-imports the real contracts from `src/api/*`** (`TodaySummary`, `MemoryPhoto`, `FamilySummary`, `ChatMessage`). A backend contract change now breaks `yarn build` instead of silently drifting the tests. |
| `tsconfig.e2e.json` | Added to the project references so `yarn build` (`tsc -b`) type-checks the E2E suite too, preserving this repo's "the build **is** the type check" convention. |

### Specs and what each actually asserts
The central design choice: **"the section rendered" is a worthless assertion in this app.** `Today.tsx` (and siblings) render a static `FALLBACK_SUMMARY` through byte-identical markup when the fetch fails, so a visibility check passes just as happily on a completely broken backend. Every spec therefore calls the API itself and asserts the screen matches *that* response.

| Spec | Tests | Asserts |
|---|---|---|
| `landing.spec.ts` | 2 | Splash auto-runs `demoLogin` for both roles, no `demo-login` response is non-2xx, URL lands on `/parent/home`, **both** role tokens are persisted under `remine-auth`, the splash logo is gone, no `불러오는 중...` remains, and the body carries >100 chars of real copy. Second test drives the bottom tab bar to 오늘 and 추억. |
| `parent-today.spec.ts` | 2 | Fetches `/api/v1/activities/today`, then for each of sleep/steps/outing/social asserts the rendered value equals the API-derived value (digits compared numerically so the browser's thousands-separator locale can't cause flakes), the percent is a real 0–100 number, and **the progress bar's inline `width` tracks that exact percent**. Plus an explicit anti-regression check that the four rendered values are *not* the static fallback set. Second test covers the 생활 패턴 분석 list. |
| `parent-gallery.spec.ts` | 2 | Card count equals the real photo count; each card's title and `YYYY년 M월` date match the API row. Second test asserts **`naturalWidth > 0` on every `<img>`** and that no image request 404s. |
| `child-family.spec.ts` | 2 | All three previously-mocked sections against live data: `family-stats` digits equal `/family/summary`'s `messageCount`/`sharedPhotoCount`/`callCount`; recent chat equals the two newest real thread messages; shared photos match the gallery head **and decode**. Second test asserts the child screen shows the *parent's* name (윤정아님) — i.e. the fetch was signed with the child's token. |
| `child-message.spec.ts` | 3 | Existing history renders at the real thread length, a uniquely-tagged message is typed and submitted, appears as the child's own bubble (`data-mine="true"`), clears the input, bumps the bubble count by exactly one — and is then **read back out of the backend** to prove it was a real write, not optimistic local state. Plus a quick-reply send and a navigation test from the 가족 tab. |

### Verification evidence
Final run, headless, after a clean `yarn build` and `yarn lint`:

```
BUILD OK
$ oxlint
Done in 0.30s.
$ playwright test
Running 11 tests using 1 worker
  ✓   1 child-family.spec.ts:10  › summary stats, recent chat and shared photos all come from the backend (1.8s)
  ✓   2 child-family.spec.ts:52  › the paired parent is resolved from the child session, not the parent one (1.6s)
  ✓   3 child-message.spec.ts:6  › sending a message appends it to the thread and persists it (1.6s)
  ✓   4 child-message.spec.ts:35 › a quick reply sends its own label as a message (1.6s)
  ✓   5 child-message.spec.ts:56 › the message screen is reachable from the 가족 tab (1.7s)
  ✓   6 landing.spec.ts:5        › splash auto-logs in both roles and lands on a rendered parent home (1.4s)
  ✓   7 landing.spec.ts:36       › the bottom tab bar navigates between the parent screens (1.6s)
  ✓   8 parent-gallery.spec.ts:11› renders one card per backend photo, with its real title and date (1.6s)
  ✓   9 parent-gallery.spec.ts:34› every photo actually decodes — a broken photoUrl must fail here (1.6s)
  ✓  10 parent-today.spec.ts:15  › the activity summary renders the backend values, not the static fallback (1.6s)
  ✓  11 parent-today.spec.ts:62  › the 생활 패턴 분석 list mirrors the same four metrics (1.6s)
  11 passed (18.0s)
```

Zero skipped, zero flaky. Run three times end-to-end (twice consecutively, once after the final refactor) with identical results — which also confirms the message specs are re-run-safe despite writing real rows each time.

**Negative control**: an assertion that has never failed is an assertion you haven't tested. Before committing, a throwaway spec intercepted `/api/v1/memories` and rewrote every `photoUrl` to a missing file; `expectImagesLoaded` failed with `<img src="/assets/does-not-exist.png"> never decoded — the URL is broken or 404s`. This is the same class of regression that was found and fixed earlier this session (`828d365`), so it is now genuinely covered rather than nominally covered. Scratch spec deleted afterwards.

### Changed in product code
Four screens gained `data-testid` hooks (`today-summary-*`/`today-pattern-*`, `memory-card`/`memory-title`/`memory-date`, `family-stats`/`stat-*`/`recent-chat-body`/`shared-photo`, `message-bubble` + `data-mine`), and `Today.tsx`'s row objects gained a stable `metric` key. Judgment call: testids are a small, invisible, zero-behavior addition, and the alternative — matching on Tailwind utility classes — produces a suite that breaks on any styling change. No rendering, styling, or logic was altered.

### Findings surfaced while building this
1. **`GET /api/v1/activities/recommendation` currently returns a 500** (`INTERNAL_ERROR`) for the seeded parent. Root cause is environmental, not a code defect: the endpoint synchronously calls OpenAI via `OpenAiActivityRecommendationGenerator`, and with no `OPENAI_API_KEY` set locally `OpenAiClient`'s blank-key guard throws `OpenAiClientException`, which `GlobalExceptionHandler` has no mapping for and so catches as a generic 500. The frontend degrades gracefully (`Today.tsx` catches it and keeps its static headline), which is why nothing looks broken. **Worth noting for follow-up**: an unavailable AI provider shouldn't surface as a 500 — mapping `OpenAiClientException` in `GlobalExceptionHandler`, or returning the `DEFAULT_PARENT_MESSAGE` on generator failure, would be the honest behavior. Not fixed here: it's a backend behavior change outside this task's scope. The specs deliberately do not assert on the AI headline, since it depends on an external API key.
2. **`E2E_BASE_URL` is effectively restricted to ports 5173/5174**, because `CorsConfig` whitelists exactly those two origins. Discovered by testing the webServer auto-start path on port 5199 and watching demo-login fail. Rather than leave that as a 20-second blind timeout, `bootDemoSession` now detects Splash's fallback to `/onboarding` and raises a message naming the CORS constraint and the offending origin.
3. **A suspected message-ordering bug turned out not to be one.** `GET /messages/thread` returns newest-first, and both `Family.tsx`'s "최근 대화" and `Message.tsx`'s bubble list read the array head-to-tail — which looked wrong until `src/api/message.ts:24` showed `getThread` already `.reverse()`s into chronological order. Checked before writing an assertion around it; recorded here because the next reader will have the same suspicion.
4. **Direct `/child/*` URL entry works, but on a narrow margin.** Entering a child route by URL while `activeRole` is still the persisted `parent` relies on `App`'s path→role effect landing before the screen's own fetch is signed (it does, because axios request interceptors run in a microtask after React's synchronous effect flush). A `switchRole` test helper written to sidestep this was **deleted** after probing the real behavior, so the suite exercises the genuine direct-URL path and would catch it if that ordering ever broke. `child-family.spec.ts:52` is the assertion that guards it.

### Delegated
Nothing. Antigravity was available and is a good fit for repetitive selector scaffolding, but this suite is small (5 specs) and almost every line was a judgment call about what constitutes a non-superficial assertion — exactly the work the task brief said to keep direct. Task 2's experience reinforced the choice: Antigravity's mechanical edit there was correct but its self-report missed formatting damage, so anything delegated needs a full diff review anyway, which would have cost more than writing 5 short specs.

## Blockers / decisions log
- **Task 1** — skipped a `@SpringBootTest` context-loads test for `app-api`: zero precedent in the codebase, and the dev datasource is a file-backed H2 the running dev server may hold open. Noted rather than inventing a first-of-its-kind test profile unsupervised.
- **Task 2** — left the `FamilySummaryController` 1000-message magic cap, the overlay-only notification routes, and the three near-duplicate parent/child page pairs alone. Each needs new API surface or a product decision, not a mechanical edit.
- **Task 3** — did not apply `@Profile("!prod")` to the unauthenticated `demo-login` endpoint. It is the correct eventual fix, but applying it autonomously would silently break a prod-profile demo deploy.
- **Task 4** — did not fix the `/activities/recommendation` 500 (backend behavior change, out of scope) and did not assert on the AI-generated headline (depends on an external API key).
- **Environment** — backend (:8080), dev servers (:5173, :5174) and redis were already running and were left running, per instruction, so the app is poke-able on waking.

## Commits made this run
14 commits, all pushed to `origin/main`.

| # | Commit | Task |
|---|---|---|
| 1 | `7bb3c7b` Add overnight mission report skeleton | — |
| 2 | `58d92b0` Add test coverage for common and auth modules | 1 |
| 3 | `266a2cf` Add test coverage for client-openai and app-api modules | 1 |
| 4 | `08725aa` Update overnight mission report: Task 1 backend test coverage complete | 1 |
| 5 | `5a6ce5a` Mark Task 1 done, Task 2 starting in overnight mission report | 1 |
| 6 | `496aa51` Remove stale signup TODO and fix useEffect exhaustive-deps warnings | 2 |
| 7 | `0dd85d8` Consolidate duplicated counterpart-resolution onto RemineUserPrincipal | 2 |
| 8 | `9dd09f6` Hoist duplicated ApiEnvelope/unwrap into the shared http module | 2 |
| 9 | `e80062f` Record Task 2 code review and cleanup results in overnight report | 2 |
| 10 | `6ba2007` Close call-start IDOR and harden JWT secret, input bounds | 3 |
| 11 | `6bee58d` Record Task 3 security review results in overnight report | 3 |
| 12 | `45fb244` Add Playwright E2E harness for the frontend | 4 |
| 13 | `c6b1b22` Cover the five core flows with E2E specs | 4 |
| 14 | _this report_ | 4 |
