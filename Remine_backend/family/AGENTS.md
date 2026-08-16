<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# family

## Purpose
Owns the family feed: parent/child pairs post short updates (optionally with a photo), like each other's posts, and reply in threaded comments. There is no separate "family pairing" entity in this module — pairing is resolved upstream via `RemineUserPrincipal.parentUserId()` / `principal.counterpartUserId()` (from `auth`), and every write/read here is scoped to that resolved `pairUserIds` set so a user can only see or act on posts authored within their own pair.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/family/domain/FamilyPost.kt` | Post entity: author, body, optional photo/caption, denormalized `likeCount` |
| `src/main/kotlin/com/remine/family/domain/FamilyPostLike.kt` | Like record (post + user) |
| `src/main/kotlin/com/remine/family/domain/FamilyPostReply.kt` | Threaded reply on a post |
| `src/main/kotlin/com/remine/family/domain/PostWithViewerState.kt` | Read-model wrapper combining a post with the requesting viewer's `likedByViewer` flag and `replyCount` |
| `src/main/kotlin/com/remine/family/application/service/FamilyPostService.kt` | Single service implementing all 5 CQRS ports; enforces `requireOwnPair` authorization on every post-scoped operation |
| `src/main/kotlin/com/remine/family/adapter/presentation/web/FamilyPostController.kt` | REST controller; resolves `pairUserIds` from the authenticated principal on every request |
| `src/main/kotlin/com/remine/family/adapter/infrastructure/jpa/FamilyPostJpaRepository.kt` | Custom cursor-based feed queries (`findFeedWithCursor` / `findFeedWithoutCursor`) |
| `src/main/kotlin/com/remine/family/adapter/infrastructure/jpa/PostReplyCountProjection.kt` | JPA projection interface for bulk reply-count aggregation (avoids N+1 when rendering the feed) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/family/domain` | Entities and one read-model VO (`PostWithViewerState`); no framework deps |
| `src/main/kotlin/com/remine/family/application/port/inbound` | CQRS Command/Query interfaces with `In`/`Out` |
| `src/main/kotlin/com/remine/family/application/port/outbound` | Repository port interfaces |
| `src/main/kotlin/com/remine/family/application/service` | `FamilyPostService`, the single implementation of all inbound ports |
| `src/main/kotlin/com/remine/family/adapter/presentation/web` | `FamilyPostController` plus request/response DTOs |
| `src/main/kotlin/com/remine/family/adapter/infrastructure/jpa` | JPA entities, Spring Data repositories, and port adapters |

## For AI Agents

### Working In This Directory
- Every command/query that targets an existing post must call `requireOwnPair(post, pairUserIds)` before proceeding — this is the module's only authorization mechanism and it throws `ForbiddenException` from `common`. Don't bypass it.
- `pairUserIds` is computed once per request in `FamilyPostController.resolvePairUserIds()` from the principal (self + parent + counterpart, all optional/nullable-safe via `setOfNotNull`). Reuse that pattern rather than re-deriving pairing logic elsewhere.
- `FamilyPost.likeCount` is a denormalized counter mutated via `post.copy(likeCount = ...)` inside `ToggleFamilyPostLikeCommand` — keep it in sync with the actual `FamilyPostLike` rows if you touch like logic.
- REST endpoints under `/api/v1/family/posts`: `POST` (create), `GET` (feed, cursor-paginated by `Instant`), `PATCH /{id}/like` (toggle), `POST /{id}/replies` (create reply), `GET /{id}/replies` (list replies) — matches the repo's create=POST/update=PATCH convention.
- All three JPA entities use `@Where(clause = "deleted_at IS NULL")` and extend `BaseOrmEntity` (soft delete). Likes are hard-removed from the port's perspective (`delete(like)`) but the adapter actually calls `entity.softDelete()` — don't assume `delete` means a hard DB delete.

### Testing Requirements
- `src/test/kotlin/com/remine/family/application/service/FamilyPostServiceTest.kt` (331 lines) covers `FamilyPostService` across all 5 operations, including the `requireOwnPair` authorization failure paths.
- `src/test/kotlin/com/remine/family/adapter/presentation/web/FamilyPostControllerTest.kt` (172 lines) covers the controller/DTO layer, including `resolvePairUserIds` behavior for parent vs. child principals.
- No JPA/repository-adapter or integration tests exist yet for this module.

### Common Patterns
- One `@Service` class implements all of a module's inbound Command/Query interfaces (`FamilyPostService : CreateFamilyPostCommand, ToggleFamilyPostLikeCommand, ...`), with `@Transactional(readOnly = true)` at the class level and `@Transactional` overridden per write method.
- Bulk read optimization: the feed query fetches posts once, then does two batched follow-up lookups (`findLikedPostIds`, `countRepliesByPostIds`) keyed by the full `postIds` collection instead of per-post queries.

## Dependencies

### Internal
- `common` (via `api`) — base entities, `ApiResponse`, shared exceptions (`EntityNotFoundException`, `ForbiddenException`)
- `auth` — `RemineUserPrincipal`, principal pairing resolution

### External
- `spring-boot-starter-security` — method-level auth / `@AuthenticationPrincipal`
- `spring-boot-starter-data-jpa` — persistence
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-validation` — `@Valid` request DTOs
- `com.h2database:h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
