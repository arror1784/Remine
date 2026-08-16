<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# auth

## Purpose
Owns request-time authentication: the stateless JWT filter chain, JWT issuance/parsing, the `RemineUserPrincipal` resolved from token claims (including parent/child pairing logic), the `PARENT`/`CHILD` role model, Google OAuth client-credentials config, and a JSON-formatted 401 entry point. It does not own signup/login business logic or the OAuth callback handler itself — those live in the `user` module, since they need to create/look up a `User` row; this module only holds the client credentials config and the security infrastructure other modules depend on.

## Key Files
| File | Description |
|------|-------------|
| `config/SecurityConfig.kt` | `@EnableWebSecurity @EnableMethodSecurity` filter chain: stateless sessions, CSRF disabled, only decides authenticated-vs-not (no path-based role rules — authorization is `@PreAuthorize`-only per root CLAUDE.md), registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter` |
| `config/CustomAuthenticationEntryPoint.kt` | Writes a JSON `ApiResponse.fail("UNAUTHORIZED", ...)` body with 401 status instead of Spring's default entry point |
| `jwt/JwtTokenProvider.kt` | Issues (`generateToken`) and parses (`parse`) HS256 JWTs; claims are `sub`=userId, `role`, `pairedUserId`; secret/expiration come from `jwt.secret` / `jwt.expiration-ms` (default 7 days) properties |
| `jwt/JwtAuthenticationFilter.kt` | `OncePerRequestFilter` reading the `Authorization: Bearer` header, parsing it via `JwtTokenProvider`, and populating `SecurityContextHolder` with a `ROLE_{PARENT|CHILD}` authority |
| `domain/RemineUserPrincipal.kt` | `userId` + `role` + `pairedUserId`, resolved once per request from JWT claims (never re-derived from DB mid-request); `parentUserId()` / `counterpartUserId()` implement the parent/child pairing indirection used across other modules |
| `domain/Role.kt` | `enum class Role { PARENT, CHILD }` |
| `oauth/GoogleOAuthProperties.kt` | `@ConfigurationProperties(prefix = "google.oauth")` holding `clientId`/`clientSecret`/`redirectUri`, sourced from Vault-injected `GOOGLE_OAUTH_CLIENT_ID`/`GOOGLE_OAUTH_CLIENT_SECRET` env vars |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `config/` | Spring Security filter chain and the custom 401 entry point |
| `jwt/` | Token issuance, parsing, and the request-scoped authentication filter |
| `domain/` | `RemineUserPrincipal` and `Role` — no Spring/JPA dependencies |
| `oauth/` | Google OAuth client credentials configuration only (not the OAuth flow itself) |

## For AI Agents

### Working In This Directory
- `SecurityConfig`'s `authorizeHttpRequests` only carries a short public allowlist — `OPTIONS /**`, `/api/v1/auth/**`, `/api/v1/users/signup`, `/h2-console/**`, `/actuator/health`, and the Swagger paths — everything else requires authentication. Per-endpoint authorization is done exclusively via `@PreAuthorize` on controller methods in other modules; do not add new path-based rules to this filter chain.
- The `OPTIONS /**` permit-all exists specifically so CORS preflight requests survive Spring Security running before the MVC-level `CorsConfig` in `app-api` — see the comment in `SecurityConfig.kt`. Don't remove it when touching security rules.
- `RemineUserPrincipal` is immutable and built once from JWT claims per request; `parentUserId()` throws `InvalidRequestException` if a CHILD principal has no `pairedUserId` yet (unpaired child account) — every other module's controllers rely on this method (not `principal.userId` directly) to resolve "whose data is this."  `counterpartUserId()` is the inverse: "who should a message/call from this principal go to."
- `pairedUserId` is baked into the JWT at issuance time (`JwtTokenProvider.generateToken`); if a child gets paired/unpaired after a token was already issued, the caller must re-login to pick up the change — this is a documented accepted MVP limitation in `RemineUserPrincipal.kt`, not a bug to silently "fix" with a DB lookup.
- `jwt.secret` and `jwt.expiration-ms` are `@Value`-injected Spring properties (Vault-backed per root CLAUDE.md), not hardcoded; `GoogleOAuthProperties` follows the same Vault-injection convention.
- This module intentionally does not contain the OAuth login/callback endpoint or a `User`/persistence layer — don't add one here; extend the `user` module instead and depend on `auth`'s principal/role types from there.

### Testing Requirements
No tests yet under `src/test/kotlin` for this module (`activity`'s `ActivityControllerTest` does exercise `RemineUserPrincipal`/`Role` from this module, but there are no auth-module-owned test files).

### Common Patterns
- This module has no `application`/`port` layers of its own (it's infrastructure, not a CQRS domain module) — just `domain` (principal/role), `config` (Security wiring), `jwt` (token handling), and `oauth` (properties only).
- Downstream modules consume this module's types via `@AuthenticationPrincipal principal: RemineUserPrincipal` on controller methods (see `activity`'s `ActivityController`, `app-api`'s composition controllers) rather than reading `SecurityContextHolder` directly.

## Dependencies

### Internal
`common` (api)

### External
- `spring-boot-starter-security` (api) — filter chain, `@PreAuthorize`, `BCryptPasswordEncoder`
- `spring-boot-starter-oauth2-client` — Google OAuth client support
- `spring-boot-starter-web` — servlet filter types
- `io.jsonwebtoken:jjwt-api:0.11.5` (+ runtime `jjwt-impl`, `jjwt-jackson`) — JWT issuance/parsing

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
