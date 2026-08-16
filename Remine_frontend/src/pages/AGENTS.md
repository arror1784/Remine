<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# pages

## Purpose
Top-level screen tree for the Remine phone-frame app. `App.tsx` (`src/App.tsx`) owns a single `<Routes>` tree split by URL prefix into three areas: unauthenticated/setup screens at the root (`Splash`, `Login`, `OnboardingFlow`, `SwitchMode`), the `parent/*` role (older-adult, "부모님" experience) and the `child/*` role (caregiver, "자녀" experience). Every route lives in one flat `<Routes>` list — there's no nested `<Outlet>` layout routing; each page composes its own `Screen`/`ModeBar`/`BottomTabBar` shell. A second, parallel `<Routes>` block renders "overlay" screens (bottom sheets and reminder cards) on top of the first when `location.state.backgroundLocation` is present, using react-router's standard modal-overlay pattern.

## Key Files
| File | Description |
|------|-------------|
| `Login.tsx` | Email/password (or equivalent) sign-in screen, entry point before onboarding/role selection. |
| `Splash.tsx` | Initial loading/branding screen shown before auth state resolves. |
| `SwitchMode.tsx` | Overlay screen for switching between an already-onboarded parent and child session (see `useAuthStore` `activeRole`/`sessions`). |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `parent/` | Screens for the older-adult ("부모님") role — home, today's analysis, mypage, memories, family, reminders. See `parent/AGENTS.md`. |
| `child/` | Screens for the caregiver ("자녀") role — mirrors most of `parent/` plus memory-contribution flows. See `child/AGENTS.md`. |
| `onboarding/` | Multi-step onboarding flow (`OnboardingFlow.tsx` plus `WelcomeStep`, `RoleSelectStep`, `ProfileStep`, `DetailStep`, `DoneStep`) and shared `types.ts` (`Role`, `ROLE_COLOR`, `AGE_GROUPS`, `PARENT_INTERESTS`, `OnboardingState`, `PAIRING_FAILED`). |

## For AI Agents

### Working In This Directory
`App.tsx` renders two `<Routes>` blocks from the same `<PhoneFrame>`:
1. The "background" tree, rendered at `backgroundLocation ?? location`, containing every real screen (`/parent/home`, `/child/today`, etc.). This tree gets `blur-sm pointer-events-none` applied to its wrapping `<div>` whenever an overlay is open.
2. The "overlay" tree, rendered only `{backgroundLocation && (...)}`, at the *actual* `location` — `SwitchMode`, the three `parent/reminders/*` sheets, `parent/Notifications`, `child/memories/AddPhoto`, and `child/Notifications`.

A `Link` opens an overlay by passing `state={{ backgroundLocation: location }}` (see `ParentHome`'s notification bell link, or `ChildFamily`'s "add photo" link). React Router then renders the background route at its last real URL while layering the overlay route on top at the new URL — the background page **does not unmount or remount**, and its own `useLocation()` does not change when the overlay opens or closes.

**The concrete gotcha**: `ParentHome.tsx`/`ChildHome.tsx` show an unread-notification badge (`unreadCount` from `useNotificationStore`) next to the bell icon that opens `parent/Notifications` / `child/Notifications` as an overlay. Reading a notification inside that overlay calls `decrementUnreadCount()` on the *shared* `useNotificationStore` (`src/store/notifications.ts`) rather than setting local state — because `Home` stays mounted the whole time, only a shared store write (not a component remount or a `useEffect` keyed on location) will cause the badge to update when the sheet closes. Any new overlay that needs to affect a background screen must follow the same rule: put the mutable state in a Zustand store, not local `useState` in either page.

Every page also imports `COLORS` from `@/theme` for inline styles (bar colors, dot colors) rather than hardcoding hex — consistent with the single-color-source rule in the root `CLAUDE.md`.

### Testing Requirements
No test files exist anywhere under `src/` yet (`*.test.*` / `*.spec.*` — none found). Vitest is configured in the project but unused so far for pages.

### Common Patterns
- Screens with server data follow a `loading` / `failed` / empty-array boolean-state trio driven by a `useEffect` that fetches on mount, guarded with a `cancelled`/`active` flag to avoid setting state after unmount.
- Fallback-to-static-copy: cards that show AI-generated text (e.g. `ParentHome`'s recommendation message, `ChildHome`'s status message) keep a hardcoded Korean fallback string and only replace it once the fetch resolves, so the UI never flashes empty.
- Reminder/suggestion screens (`parent/reminders/*`) are thin wrappers around a shared `SuggestionSheet` component, differing only in emoji/copy/color/destination.
- Role-styling lookup tables keyed by `UserResponse['role']` (`{ PARENT: {...}, CHILD: {...} }`, called `ROLE_STYLE`) are duplicated across `Family`, `Message`, and `Call` pages in both `parent/` and `child/`, built from `ROLE_COLOR` in `onboarding/types.ts`.

## Dependencies

### Internal
- `@/store/auth` (`useAuthStore`) — active role, per-role sessions, logout.
- `@/store/notifications` (`useNotificationStore`) — shared unread-count badge state, read across the backgroundLocation boundary.
- `@/components/PhoneFrame`, `Screen`, `ModeBar`, `BottomTabBar`, `BottomSheet`, `CallScreen`, `SuggestionSheet` — shared chrome components used by nearly every page.
- `@/theme` (`COLORS`) — single color-token source.

### External
- `react-router-dom` — `Routes`/`Route`/`Link`/`useLocation`/`useNavigate`/`useParams`, plus the `state.backgroundLocation` overlay convention described above.
- `zustand` — backs both stores above.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
