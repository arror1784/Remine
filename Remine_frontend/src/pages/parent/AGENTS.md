<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# parent

## Purpose
Screens for the older-adult ("부모님") role. This role is the person being cared for: it surfaces AI-summarized activity ("오늘의 활동"), lets them respond to daily reminders (walk/call/quiz), run reminiscence quizzes built from family-submitted photos, and talk to their paired family member. Every top-level screen here uses the pink accent (`COLORS.pink`) and is wrapped in `<Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>` with a `<ModeBar label="부모님 모드 — {name}님" color={COLORS.pink} />` header band.

## Key Files
| File | Description |
|------|-------------|
| `Home.tsx` | Main dashboard: greeting, 7-day activity bar chart, per-metric activity bars (sleep/steps/outings/social) built from `getTodaySummary`, an AI-recommendation card (`getRecommendation`) with a CTA into the matching reminder screen, and an "오늘의 추억" card previewing today's quiz photo (`getTodayQuiz`). Uses `api/activity.ts` and `api/memory.ts`, and `store/notifications` for the bell badge. |
| `Today.tsx` | "오늘의 분석" — a fuller breakdown of the day's activity summary, per-metric pattern list, and a list of suggested activities linking into the three `reminders/*` overlay screens. Currently renders entirely from hardcoded `SUMMARY`/`PATTERNS`/`SUGGESTIONS` constants — no `api/*` calls. |
| `MyPage.tsx` | Profile/settings screen: profile card, streak/quiz/shared-photo stats, weekly completion strip, and static account/app/info menu sections. Uses `api/family.ts` (`getMyProfile`) and `api/mypage.ts` (`getMyPageStats`, `getWeeklyPattern`); logs out via `useAuthStore().clearSessions()`. |
| `Notifications.tsx` | Overlay bottom sheet (rendered via `BottomSheet`) listing notifications from `api/notification.ts` (`getNotifications`, `markNotificationAsRead`). Tapping an item marks it read, decrements the shared `useNotificationStore` unread count for every other notification sharing the same `deepLink`, and navigates to `/parent/{deepLink}`. |
| `Family.tsx` | Family feed: paired-profile lookup, family activity summary, a scrollable post feed with like/reply, and a "leave a message" composer. Uses `api/family.ts` heavily (`getMyProfile`, `getPairedProfile`, `getFamilySummary`, `listPosts`, `listReplies`, `createPost`, `createReply`, `toggleLike`). Shows an empty state when no paired family member exists yet. |
| `Message.tsx` | 1:1 chat thread with the paired family member. Uses `api/message.ts` (`getThread`, `getQuickReplies`, `sendMessage`) and `api/family.ts` (`getPairedProfile`); polls `getThread()` every 4s (no websocket/push infra yet) once a paired profile is found. |
| `Call.tsx` | Full-screen call UI (delegates rendering to shared `CallScreen` component) for calling the paired family member. Uses `api/family.ts` (`getPairedProfile`) only. |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `memories/` | Photo gallery (`Gallery.tsx`) and the reminiscence quiz flow (`Quiz.tsx`). |
| `reminders/` | Thin `SuggestionSheet` wrapper screens (`WalkReminder.tsx`, `CallReminder.tsx`, `QuizReminder.tsx`) shown as overlays, each pointing at a real action screen. |

### `memories/`
| File | Description |
|------|-------------|
| `Gallery.tsx` | Grid of past memory photos plus a "오늘의 추억 퀴즈" banner linking to `Quiz.tsx`. Currently renders entirely from a hardcoded `MEMORIES` array (bundled asset imports) — no `api/memory.ts` calls yet, unlike `Home.tsx`'s use of `getTodayQuiz`. |
| `Quiz.tsx` | Multiple-choice quiz flow for the day's photo: fetches `getTodayQuiz()`, steps through `questions`, submits all answers at once via `submitQuizAttempt(photo.id, answers)` once every question is answered, then shows a scored completion screen with a retry-on-failure path. |

### `reminders/`
| File | Description |
|------|-------------|
| `WalkReminder.tsx` | `SuggestionSheet` prompting a walk; primary action navigates to `/parent/home` (no dedicated walk-tracking screen exists). |
| `CallReminder.tsx` | `SuggestionSheet` prompting a family call; primary action navigates to `/parent/family/call`. |
| `QuizReminder.tsx` | `SuggestionSheet` prompting today's memory quiz; primary action navigates to `/parent/memories/quiz`. |

## For AI Agents

### Working In This Directory
- `Notifications.tsx`, and all three `reminders/*` screens, are only ever reached as **overlay** routes (`state: { backgroundLocation }`) per the pattern documented in `../AGENTS.md`. `Home.tsx` stays mounted underneath them, so any state that must reflect what happened inside (e.g. the unread badge) has to go through `useNotificationStore`, not local component state — see `Notifications.tsx`'s use of `decrementUnreadCount`.
- `Home.tsx` and `Today.tsx` currently diverge in data-source maturity: `Home.tsx` is wired to `getRecommendation`/`getTodaySummary`/`getTodayQuiz`, while `Today.tsx` and `memories/Gallery.tsx` still render from local hardcoded constants. Treat the hardcoded ones as pending real-API wiring, not as an intentional design choice, when picking up related work.
- `MyPage.tsx`'s week-day-label helper (`weekdayLabel`) manually parses `YYYY-MM-DD` into `new Date(year, month-1, day)` instead of `new Date(str)`, specifically to avoid a UTC-midnight day-shift bug in negative-offset timezones — reuse that pattern rather than `new Date(statDate)` if you add more calendar-date formatting here.

### Testing Requirements
No test files exist under `parent/` (or anywhere in `src/`) yet.

### Common Patterns
- `loading` / `failed` boolean state pair per screen, set from a `useEffect` fetch with a `cancelled` flag guard, with three render branches: loading message, failed message, and success content.
- Screens with a 1:1 paired family member (`Family.tsx`, `Message.tsx`, `Call.tsx`) all call `getPairedProfile()` first and render a "아직 연결된 가족이 없어요" empty state when it resolves to `null`, before attempting any further data fetch.
- `Message.tsx` polls on a `setInterval` rather than using a store or websocket, explicitly noted in-code as a stopgap until real-time infra exists.
- No ST/DR dual-buffer editing appears in this directory — all forms here (reply drafts, message composer) are single-shot submit-and-clear, not saved-value/draft-value pairs.

## Dependencies

### Internal
- `@/api/activity.ts` — `getRecommendation`, `getTodaySummary` (`Home.tsx`).
- `@/api/memory.ts` — `getTodayQuiz`, `submitQuizAttempt` (`Home.tsx`, `memories/Quiz.tsx`).
- `@/api/family.ts` — `getMyProfile`, `getPairedProfile`, `getFamilySummary`, `listPosts`, `listReplies`, `createPost`, `createReply`, `toggleLike` (`MyPage.tsx`, `Family.tsx`, `Message.tsx`, `Call.tsx`).
- `@/api/mypage.ts` — `getMyPageStats`, `getWeeklyPattern` (`MyPage.tsx`).
- `@/api/notification.ts` — `getNotifications`, `markNotificationAsRead` (`Notifications.tsx`).
- `@/api/message.ts` — `getThread`, `getQuickReplies`, `sendMessage` (`Message.tsx`).
- `@/store/notifications` (`useNotificationStore`) — `Home.tsx`, `Notifications.tsx`.
- `@/store/auth` (`useAuthStore`) — `MyPage.tsx` (logout), `Message.tsx` (`myUserId` for bubble alignment).
- `@/pages/onboarding/types` (`ROLE_COLOR`) — role-based emoji/color lookups in `Family.tsx`, `Message.tsx`, `Call.tsx`.
- `@/components/*` — `Screen`, `ModeBar`, `BottomTabBar`, `BottomSheet`, `CallScreen`, `SuggestionSheet`, `icons/NavIcons` (`BellIcon`).
- `@/theme` (`COLORS`).

### External
- `react-router-dom` — `Link`, `useLocation`, `useNavigate`.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
