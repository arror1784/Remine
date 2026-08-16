<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# child

## Purpose
Screens for the caregiver ("자녀") role. This role monitors the paired parent's daily activity and cognitive health, sends encouragement/messages/calls, and contributes photos + verified answers that become the parent's reminiscence quizzes. Every top-level screen here uses the blue accent (`COLORS.blue`) and is wrapped in `<Screen footer={<BottomTabBar role="child" accentColor={COLORS.blue} />}>` with a `<ModeBar label="자녀 모드 — {name}님" color={COLORS.blue} />` header band — the same shell pattern as `parent/`, just recolored.

## Key Files
| File | Description |
|------|-------------|
| `Home.tsx` | Dashboard summarizing the parent's day: status card with message/call CTAs, an AI "상태 알림" card (`getRecommendation().childMessage`), a per-metric activity list with ok/warning badges built from `getTodaySummary`, a 7-day pattern chart, and horizontally-scrollable memory-photo shortcuts. Uses `api/activity.ts` and `store/notifications`. |
| `Today.tsx` | "어머니의 오늘" — a checklist of the parent's daily activities (sleep/breakfast/walk/quiz) with a "보내기" cheer button per incomplete item, plus an activity timeline. Renders entirely from hardcoded `CHECKLIST`/`TIMELINE` constants; only the cheer-sent local state (`cheeredIds`) is interactive — no `api/*` calls. |
| `MyPage.tsx` | Profile/settings screen: profile + paired-parent connection card, month stats (photos/messages/calls) from `getFamilySummary`, toggleable notification-alert switches (local state only, not persisted), and static account/app-info menu sections. Uses `api/family.ts` (`getMyProfile`, `getPairedProfile`, `getFamilySummary`); logs out via `useAuthStore().clearSessions()`. |
| `Notifications.tsx` | Overlay bottom sheet, byte-for-byte the same structure as `parent/Notifications.tsx` (same `BottomSheet`, same `api/notification.ts` calls, same shared-`deepLink` read-clearing logic), differing only in the blue accent color and the `/child/{deepLink}` navigate target. |
| `Family.tsx` | Family feed for the child side: paired-profile + summary cards, a short hardcoded "최근 대화" preview (`RECENT_CHAT`, not fetched from `api/message.ts`) linking to the full `Message.tsx` thread, and a hardcoded shared-photos strip (`SHARED_PHOTOS`) plus an "add photo" tile linking to `memories/AddPhoto.tsx` as an overlay. Uses `api/family.ts` (`getPairedProfile`, `getFamilySummary`) only — no post/reply feed like the parent version. |
| `Message.tsx` | 1:1 chat thread with the paired parent — structurally identical to `parent/Message.tsx` (same `api/message.ts` calls, same 4s polling, same quick-replies), differing only in blue bubble color and `/child/family/call` link target. |
| `Call.tsx` | Full-screen call UI via shared `CallScreen`, identical in structure to `parent/Call.tsx`; the only behavioral difference is the `relation` label (`'부모님'` when the paired user's role is `PARENT`, vs. `parent/Call.tsx`'s `'자녀'` for `CHILD`) and `backTo="/child/family"`. |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `memories/` | Photo gallery (`Gallery.tsx`) plus the two-step photo-contribution flow unique to the child role: `AddPhoto.tsx` (pick/caption a photo) → `AnswerQuiz.tsx` (supply real answers to AI-generated questions). |

### `memories/`
| File | Description |
|------|-------------|
| `Gallery.tsx` | Grid of photos the child has contributed, with per-photo "퀴즈 활용 중"/"대기 중" status badges and stat tiles (total/quiz-used/added-this-month). Renders from a hardcoded `PHOTOS` array — no `api/memory.ts` calls, unlike `AddPhoto.tsx`/`AnswerQuiz.tsx` in the same directory. |
| `AddPhoto.tsx` | Overlay bottom sheet where the child picks one of six bundled sample images (stand-in for real upload — explicitly flagged in a top-of-file Korean comment as pre-review temporary UI reusing an existing pattern), enters a title and memory-label, then calls `uploadPhoto()` followed by `generateQuizQuestions(photoId)` (`api/memory.ts`) and navigates to `AnswerQuiz.tsx`. If question generation fails, it still navigates forward — the photo is already saved, so blocking here would risk a duplicate-save on retry; the failure surfaces instead as `AnswerQuiz.tsx`'s empty state. |
| `AnswerQuiz.tsx` | Also flagged as pre-review temporary UI. Loads AI-drafted questions via `getDraftQuestions(photoId)`, collects the child's real answers per question, and submits them together via `completeQuizWithAnswers(photoId, [{questionId, answer}])`, then navigates back to `/child/memories`. Shows a dedicated empty state ("아직 만들어진 질문이 없어요") if no draft questions exist — the landing spot for `AddPhoto.tsx`'s silent quiz-generation failure. |

## For AI Agents

### Working In This Directory
- `Notifications.tsx` and `memories/AddPhoto.tsx` are only ever reached as **overlay** routes (see `../AGENTS.md` for the `backgroundLocation` mechanism). `Home.tsx` stays mounted underneath `Notifications.tsx`, so its unread badge is read from and cleared through the shared `useNotificationStore`, exactly as documented for the parent side — do not introduce local `useState` for unread state here.
- Data-source maturity is inconsistent across this directory the same way it is in `parent/`: `Home.tsx` and the `memories/AddPhoto.tsx` → `AnswerQuiz.tsx` flow are wired to real `api/*` calls, while `Today.tsx`, `Family.tsx`'s chat preview and photo strip, and `memories/Gallery.tsx` still render hardcoded constants. Two files (`AddPhoto.tsx`, `AnswerQuiz.tsx`) carry an explicit Korean top-of-file comment marking them as temporary, pre-design-review UI — expect them to be redesigned, not just re-wired.
- `AddPhoto.tsx`'s "photo picker" is six bundled asset images, not a real file input, because no upload/storage backend exists yet — don't mistake `SAMPLE_PHOTOS` for production photo-picking UX.

### Testing Requirements
No test files exist under `child/` (or anywhere in `src/`) yet.

### Common Patterns
- Same `loading`/`failed` boolean-pair + `cancelled` guard fetch pattern as `parent/`.
- Same empty-state convention for an unpaired family member ("아직 연결된 부모님이 없어요" in `Family.tsx`/`Message.tsx`/`Call.tsx`), mirroring `parent/`'s "아직 연결된 가족이 없어요".
- `memories/AddPhoto.tsx` → `memories/AnswerQuiz.tsx` is a two-step create flow chained via URL param (`/child/memories/:photoId/answer-quiz`) rather than shared component state — each step independently fetches what it needs by `photoId`.
- No ST/DR dual-buffer editing here either — `MyPage.tsx`'s alert toggles are local-only `useState`, not persisted or drafted against a saved value.

## Dependencies

### Internal
- `@/api/activity.ts` — `getRecommendation`, `getTodaySummary` (`Home.tsx`).
- `@/api/family.ts` — `getMyProfile`, `getPairedProfile`, `getFamilySummary` (`MyPage.tsx`, `Family.tsx`, `Message.tsx`, `Call.tsx`).
- `@/api/message.ts` — `getThread`, `getQuickReplies`, `sendMessage` (`Message.tsx`).
- `@/api/notification.ts` — `getNotifications`, `markNotificationAsRead` (`Notifications.tsx`).
- `@/api/memory.ts` — `uploadPhoto`, `generateQuizQuestions`, `getDraftQuestions`, `completeQuizWithAnswers` (`memories/AddPhoto.tsx`, `memories/AnswerQuiz.tsx`).
- `@/store/notifications` (`useNotificationStore`) — `Home.tsx`, `Notifications.tsx`.
- `@/store/auth` (`useAuthStore`) — `MyPage.tsx` (logout), `Message.tsx` (`myUserId`).
- `@/pages/onboarding/types` (`ROLE_COLOR`) — `Message.tsx`, `Call.tsx`.
- `@/components/*` — `Screen`, `ModeBar`, `BottomTabBar`, `BottomSheet`, `CallScreen`.
- `@/theme` (`COLORS`).

### External
- `react-router-dom` — `Link`, `useLocation`, `useNavigate`, `useParams` (`memories/AnswerQuiz.tsx`).

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
