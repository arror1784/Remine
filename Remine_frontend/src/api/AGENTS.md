<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# api

## Purpose
Domain-scoped HTTP clients for the Remine frontend. Every network call the app makes goes through one of these files, each wrapping a single backend domain (auth, activity, calls, family, memories/quiz, messages, mypage, notifications) around the shared `http` Axios instance in `http.ts`. Every endpoint response is unwrapped from a uniform `{ data, error }` envelope before being returned to callers, so component code never sees the envelope shape or raw Axios responses.

## Key Files
| File | Description |
|------|-------------|
| `http.ts` | Shared Axios instance: attaches the active session's bearer token on every request, and on any `401` clears all demo sessions and hard-redirects to `/`. |
| `auth.ts` | Demo login (`demoLogin`), invite-code pairing (`pairWithInviteCode`), and signup (`signUp`). |
| `activity.ts` | Today's AI activity recommendation and today's activity stat summary (sleep/steps/outings/social with percent-of-goal fields). |
| `call.ts` | Starts and ends a call (`startCall`, `endCall`); the callee is resolved server-side from the caller's paired counterpart. |
| `family.ts` | User profile lookups (self + paired), family feed posts (list/create/like), replies, and a family activity summary. |
| `memory.ts` | Memory photo upload, AI quiz-question generation/draft retrieval, today's quiz, and two quiz-submission flows (`submitQuizAttempt`, `completeQuizWithAnswers`). |
| `message.ts` | Chat thread retrieval (server returns newest-first, this reverses it to oldest-first for display), sending a message, and quick-reply presets. |
| `mypage.ts` | My Page stats (streak, shared photos, quiz count, weekly activity) and the weekly step pattern. |
| `notification.ts` | Notification list, marking one as read, and unread count. |

## For AI Agents

### Working In This Directory
- Never call `axios`/`fetch` directly from a component — add or extend a client here and import from `@/api/<domain>.ts`. This is what keeps auth/token-refresh centralized in `http.ts`.
- Every file defines its own local `ApiEnvelope<T>` interface and `unwrap<T>()` helper (duplicated per-file, not shared) — follow the same pattern for new files rather than importing one from elsewhere.
- `unwrap()` throws on `envelope.error` and on `null` data — callers rely on this to treat network calls as either "resolved with real data" or "threw." The one exception is `getPairedProfile()` in `family.ts`, which treats `null` data as a valid "not yet paired" result and skips `unwrap`.
- **Backend contract is camelCase** and every response type here preserves the entity's `id` field, so CRUD stays in sync with `PATCH`/`DELETE` calls keyed by that id.
- HTTP verbs follow the project convention: `POST` to create, `PATCH` to update (never `PUT`), `GET` to read. See `toggleLike`, `markNotificationAsRead`, `endCall` for `PATCH` examples.
- There is no `sync`/bulk-save endpoint in this directory yet; if a list-editing feature is added here, follow the project's bulk convention (single `PATCH /<resource>/sync` call) rather than one request per row.

### Testing Requirements
No test files exist under `src/api/` (or anywhere in `src/`) yet — no Vitest specs to run or extend.

### Common Patterns
- Local `interface ApiEnvelope<T> { data: T | null; error: { code: string; message: string } | null }` + `function unwrap<T>(envelope): T` repeated at the top of every file.
- One exported `async function` per endpoint, typed with an exported response `interface`.
- Inline comments are used sparingly, only to explain non-obvious backend behavior (e.g. `call.ts`'s note that the callee is inferred server-side, `message.ts`'s note on reversing thread order, `http.ts`'s note on why a 401 means hard logout rather than refresh).

## Dependencies

### Internal
- All files import `http` from `@/api/http.ts`.
- `http.ts` imports `useAuthStore` from `@/store/auth.ts` to read the active session token and to clear sessions on 401.
- `auth.ts` imports the `Role` type from `@/store/auth.ts`.
- Components (e.g. `CallScreen.tsx` for `call.ts`) call into these clients directly; `src/store/notifications.ts` calls `getUnreadCount` from `notification.ts`.

### External
- `axios` (only imported in `http.ts`; every other file goes through that shared instance).

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
