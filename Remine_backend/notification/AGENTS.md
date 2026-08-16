<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# notification

## Purpose
The `notification` module owns in-app notifications delivered to a single recipient user — e.g. "your daughter added a photo" or "your parent hasn't logged an outing today." Each notification carries a display-oriented payload (emoji, background color, title, description, and a `deepLink` used both for client-side navigation and, notably, as the implicit "kind" of the notification) plus a `read` flag. The module exposes read/write endpoints for a single authenticated user's own notifications: listing them, checking an unread count, and marking one as read. Marking a notification as read has a side effect: it also marks every other unread notification for that recipient with the same `deepLink` as read, on the theory that the user has now seen that context. There is no separate "notification type" field — `deepLink` doubles as it.

## Key Files
| File | Description |
|------|-------------|
| `src/main/kotlin/com/remine/notification/domain/Notification.kt` | Domain entity (id, recipientUserId, emoji, bgColor, title, description, deepLink, read, createdAt, updatedAt) with a `markAsRead()` that returns a copy |
| `src/main/kotlin/com/remine/notification/application/port/inbound/CreateNotificationCommand.kt` | Command port: creates a notification for a recipient (used by other modules/services to fire notifications) |
| `src/main/kotlin/com/remine/notification/application/port/inbound/GetNotificationsQuery.kt` | Query port: lists all notifications for a recipient |
| `src/main/kotlin/com/remine/notification/application/port/inbound/GetUnreadNotificationCountQuery.kt` | Query port: returns the unread count for a recipient |
| `src/main/kotlin/com/remine/notification/application/port/inbound/MarkNotificationAsReadCommand.kt` | Command port: marks one notification (scoped to id + recipientUserId) as read |
| `src/main/kotlin/com/remine/notification/application/port/outbound/NotificationRepositoryPort.kt` | Outbound port: findByIdAndRecipientUserId, findAllByRecipientUserIdOrderByCreatedAtDesc, countByRecipientUserIdAndReadFalse, save |
| `src/main/kotlin/com/remine/notification/application/service/CreateNotificationService.kt` | Implements `CreateNotificationCommand` |
| `src/main/kotlin/com/remine/notification/application/service/GetNotificationsService.kt` | Implements `GetNotificationsQuery` (read-only transaction) |
| `src/main/kotlin/com/remine/notification/application/service/GetUnreadNotificationCountService.kt` | Implements `GetUnreadNotificationCountQuery` (read-only transaction) |
| `src/main/kotlin/com/remine/notification/application/service/MarkNotificationAsReadService.kt` | Implements `MarkNotificationAsReadCommand`; after saving the target as read, finds all other unread notifications for the same recipient with a matching `deepLink` and marks them read too |
| `src/main/kotlin/com/remine/notification/adapter/presentation/web/NotificationController.kt` | REST controller at `/api/v1/notifications` |
| `src/main/kotlin/com/remine/notification/adapter/presentation/web/NotificationResponse.kt` | Response DTO with a `from(domain)` mapper |
| `src/main/kotlin/com/remine/notification/adapter/presentation/web/UnreadNotificationCountResponse.kt` | Response DTO wrapping `count: Int` |
| `src/main/kotlin/com/remine/notification/adapter/infrastructure/jpa/NotificationJpaEntity.kt` | JPA entity for table `notification`, soft-delete via `@Where(clause = "deleted_at IS NULL")`, extends `BaseOrmEntity` |
| `src/main/kotlin/com/remine/notification/adapter/infrastructure/jpa/NotificationJpaRepository.kt` | Spring Data repository: findByIdAndRecipientUserId, findAllByRecipientUserIdOrderByCreatedAtDesc, countByRecipientUserIdAndReadFalse |
| `src/main/kotlin/com/remine/notification/adapter/infrastructure/jpa/NotificationRepositoryAdapter.kt` | Implements `NotificationRepositoryPort`, translating between domain `Notification` and `NotificationJpaEntity` |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/kotlin/com/remine/notification/domain/` | Domain entity, no Spring/JPA dependencies |
| `src/main/kotlin/com/remine/notification/application/port/inbound/` | CQRS Command/Query interfaces with In/Out inner classes |
| `src/main/kotlin/com/remine/notification/application/port/outbound/` | Outbound repository port interface |
| `src/main/kotlin/com/remine/notification/application/service/` | Service implementations of the inbound ports |
| `src/main/kotlin/com/remine/notification/adapter/presentation/web/` | REST controller and request/response DTOs |
| `src/main/kotlin/com/remine/notification/adapter/infrastructure/jpa/` | JPA entity, Spring Data repository, and the outbound port adapter |

## For AI Agents

### Working In This Directory
- REST endpoints are all scoped to the authenticated caller via `@AuthenticationPrincipal principal: RemineUserPrincipal` — there is no endpoint to read or mutate another user's notifications, and the repository queries (`findByIdAndRecipientUserId`) enforce that at the query level too, not just in the controller.
- All three endpoints under `/api/v1/notifications` are read/update only (`GET`, `GET`, `PATCH .../read`); there is no public `POST` — notifications are created internally via `CreateNotificationCommand`, presumably called from other modules/services when a notifiable event happens (e.g. a message being sent, per recent git history), not directly by a client.
- `deepLink` is being used as an implicit notification "kind" for the auto-clear-siblings behavior in `MarkNotificationAsReadService`. If you add a real `type`/`kind` field later, keep or intentionally replace this behavior — don't silently drop it, since it's covered by tests.
- The JPA entity mutates in place for updates (`NotificationRepositoryAdapter.save` re-applies fields onto an existing managed entity when one is found by id) rather than always inserting a fresh entity, to avoid duplicate-key issues on update.

### Testing Requirements
`src/test/kotlin/com/remine/notification/` has full coverage of the module's behavior:
- `adapter/presentation/web/NotificationControllerTest.kt` — verifies the controller delegates to the correct port with the correct `In`, and wraps results in `ApiResponse` correctly, for all three endpoints.
- `application/service/CreateNotificationServiceTest.kt` — verifies a new notification is created unread with all fields set correctly.
- `application/service/GetNotificationsServiceTest.kt` — verifies notifications are scoped to the requesting recipient and returned ordered by `createdAt` descending.
- `application/service/GetUnreadNotificationCountServiceTest.kt` — verifies the count passthrough.
- `application/service/MarkNotificationAsReadServiceTest.kt` — verifies marking as read, the same-`deepLink` sibling auto-clear side effect, and both `EntityNotFoundException` cases (notification doesn't exist; notification exists but belongs to a different recipient).

All service tests use hand-written in-memory fake `NotificationRepositoryPort` implementations rather than a mocking framework or an H2-backed integration test.

### Common Patterns
- CQRS: every inbound port is a `fun handle(command/query: In): Out` interface with `In`/`Out` as nested `data class`es, one interface per file.
- Services are `@Service @Transactional` (write) or `@Service @Transactional(readOnly = true)` (read), each implementing exactly one inbound port.
- The domain `Notification.markAsRead()` and the JPA entity's `markAsRead()` both exist — the domain version is immutable (`copy`), the JPA version mutates — reflecting the adapter layer's mutable-entity ORM style vs. the domain layer's immutable style.
- Not-found cases throw `com.remine.common.domain.exception.EntityNotFoundException` from the `common` module rather than returning null/Optional up through the service layer.

## Dependencies

### Internal
- `common` (`api(project(":common"))`) — base entity, `ApiResponse`, shared exceptions
- `auth` (`implementation(project(":auth"))`) — `RemineUserPrincipal`, `Role` used by the web adapter and tests

### External
- `spring-boot-starter-security` — for `@AuthenticationPrincipal` / security context integration
- `spring-boot-starter-data-jpa` — JPA entity + Spring Data repository
- `spring-boot-starter-web` — REST controller
- `spring-boot-starter-validation` — request validation (declared; no `@Valid` request bodies currently in this module)
- `h2` (test only) — in-memory DB for tests

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
