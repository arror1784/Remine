<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# store

## Purpose
Global Zustand state for the Remine frontend. Only two stores exist today: a persisted demo-auth/session store shared by both app roles (`parent`/`child`), and an in-memory notification-badge store used to keep the unread bell count in sync across routes that don't remount.

## Key Files
| File | Description |
|------|-------------|
| `auth.ts` | `useAuthStore` — holds per-role demo sessions (`{ parent?, child? }`), the currently active role, and derives the active bearer token; persisted to storage under the key `remine-auth`. |
| `notifications.ts` | `useNotificationStore` — holds `unreadCount`, with `refreshUnreadCount()` (fetches from `@/api/notification`) and `decrementUnreadCount(by)`; not persisted. |

## For AI Agents

### Working In This Directory
- `auth.ts` is the **only** store using the `persist` middleware (localStorage key `remine-auth`). `notifications.ts` is intentionally in-memory — it exists specifically because Home stays mounted underneath the notifications bottom sheet (rendered as a background route), so the badge can't rely on remounting to pick up reads; don't "fix" this by making it persisted or by deriving it from a remount.
- `auth.ts` supports two concurrent demo sessions (one per role) at once, keyed by `Role`; `getActiveToken()` reads whichever role is currently active. Anything reading/writing the token must go through this store, not by reading storage directly — `@/api/http.ts`'s interceptor and `clearSessions()`-on-401 logic depend on it.
- `notifications.ts` swallows fetch failures in `refreshUnreadCount()` and keeps the last known count rather than resetting to 0 or throwing — preserve that behavior if you touch it.
- Neither store currently models draft vs. saved (ST/DR) state — both are live app state, not editable-form data, so the dual-buffer editing convention doesn't apply here. If a future store manages user-editable list/form data, that one should follow ST/DR.

### Testing Requirements
No test files exist under `src/store/` yet — no Vitest specs to run or extend.

### Common Patterns
- Plain `create<T>()(...)` (no middleware) for ephemeral state (`notifications.ts`); `create<T>()(persist((set, get) => ({...}), { name: '<storage-key>' }))` for state that must survive reload (`auth.ts`).
- State + actions are defined together in one interface/object — no separate selectors file.
- Async actions (`refreshUnreadCount`) wrap the API call in try/catch and update state only on success.

## Dependencies

### Internal
- `notifications.ts` imports `getUnreadCount` from `@/api/notification.ts`.
- `@/api/http.ts` imports `useAuthStore` from `auth.ts` to attach the bearer token and to clear sessions on a `401`.
- `@/api/auth.ts` imports the `Role` type from `auth.ts`.

### External
- `zustand` (`create`) in both files; `zustand/middleware` (`persist`) in `auth.ts` only.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
