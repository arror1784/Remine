<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# message

## Purpose
In-app 1:1 chat between a paired parent/child user, plus role-scoped "quick reply" templates (short canned phrases like "잘 있어💕" or "보고 싶어요 엄마 😊") that let an elderly parent reply without typing. There is no group chat or multi-party thread — every thread is exactly one parent and one child, and the recipient is always resolved server-side from the pairing on the authenticated principal, never taken from the request body. Sending a message also fires a real notification to the recipient via the `notification` module.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/message/domain/ChatMessage.kt` | Message entity: sender, recipient, body, optional `quickReplyKey` |
| `src/main/kotlin/com/remine/message/domain/QuickReply.kt` | Canned reply template scoped by `role` ("PARENT"/"CHILD") with `sortOrder` |
| `src/main/kotlin/com/remine/message/application/service/SendMessageService.kt` | Saves the message, then calls `CreateNotificationCommand` (from `notification`) to push a "새 메시지가 도착했어요" 💬 notification to the recipient with a `family/message` deep link |
| `src/main/kotlin/com/remine/message/application/service/GetChatThreadService.kt` | Cursor-paginated (`before: Instant`) thread lookup between two users |
| `src/main/kotlin/com/remine/message/application/service/GetQuickRepliesService.kt` | Role-scoped quick-reply lookup |
| `src/main/kotlin/com/remine/message/adapter/presentation/web/MessageController.kt` | REST controller; `resolveRecipientId` derives the other party from `Role.PARENT`/`Role.CHILD` via `principal.counterpartUserId()` / `principal.parentUserId()` |
| `src/main/kotlin/com/remine/message/adapter/infrastructure/seed/QuickReplySeeder.kt` | `ApplicationRunner` that seeds default PARENT and CHILD quick-reply sets on startup if none exist yet for that role |
| `src/main/kotlin/com/remine/message/adapter/infrastructure/jpa/ChatMessageJpaRepository.kt` | Custom cursor queries `findThread` / `findThreadBefore` |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/message/domain` | `ChatMessage`, `QuickReply` entities; no framework deps |
| `src/main/kotlin/com/remine/message/application/port/inbound` | CQRS Command/Query interfaces (`SendMessageCommand`, `GetChatThreadQuery`, `GetQuickRepliesQuery`) |
| `src/main/kotlin/com/remine/message/application/port/outbound` | `ChatMessageRepositoryPort`, `QuickReplyRepositoryPort` |
| `src/main/kotlin/com/remine/message/application/service` | One service per inbound port |
| `src/main/kotlin/com/remine/message/adapter/presentation/web` | `MessageController` plus request/response DTOs |
| `src/main/kotlin/com/remine/message/adapter/infrastructure/jpa` | JPA entities, Spring Data repositories, port adapters |
| `src/main/kotlin/com/remine/message/adapter/infrastructure/seed` | `QuickReplySeeder` demo/seed data |

## For AI Agents

### Working In This Directory
- Never trust a recipient ID from the request body — `resolveRecipientId(principal)` in `MessageController` is the only place recipient resolution happens, and it's role-driven (`PARENT` → `counterpartUserId()`, `CHILD` → `parentUserId()`). Any new endpoint dealing with a specific chat partner should reuse this, not accept a raw `recipientId`/`userId` param from the client.
- `SendMessageService` has a hard dependency on `notification`'s `CreateNotificationCommand` inbound port — if you change the message-sending flow, keep the notification side-effect wired (this was added in a recent commit specifically to make message notifications real rather than stubbed).
- `QuickReplyRepositoryPort.existsByRole` gates the seeder so it only seeds once per role — don't remove that check or the seeder will duplicate rows on every restart.
- REST endpoints under `/api/v1/messages`: `POST` (send), `GET /thread` (cursor-paginated history), `GET /quick-replies` (role-scoped templates) — matches root create=POST/read=GET convention; there is no update or delete endpoint for messages.

### Testing Requirements
- `src/test/kotlin/com/remine/message/SendMessageServiceTest.kt` (109 lines) — covers the send flow including the notification side-effect.
- `src/test/kotlin/com/remine/message/GetChatThreadServiceTest.kt` (45 lines) and `GetQuickRepliesServiceTest.kt` (38 lines) — query-side coverage.
- `src/test/kotlin/com/remine/message/MessageControllerTest.kt` (135 lines) — controller/recipient-resolution coverage for both roles.
- `src/test/kotlin/com/remine/message/QuickReplySeederTest.kt` (60 lines) — seeder idempotency (`existsByRole` gating).

### Common Patterns
- One `@Service` class per inbound port (same shape as `memory`), each implementing exactly one Command/Query interface.
- Cross-module side effects (notification-on-send) are called directly from the application service via another module's inbound port interface, not via an event bus — follow this same direct-port-call pattern for other module-to-module effects unless the codebase introduces an event mechanism.

## Dependencies

### Internal
- `common` (via `api`) — base entities, `ApiResponse`, shared exceptions
- `auth` — `RemineUserPrincipal`, `Role`
- `notification` — `CreateNotificationCommand`, invoked directly from `SendMessageService`

### External
- `spring-boot-starter-data-jpa` — persistence
- `spring-boot-starter-web` — REST controllers
- `spring-boot-starter-validation` — `@Valid` request DTOs
- `spring-boot-starter-security` — `@AuthenticationPrincipal`
- `com.h2database:h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
