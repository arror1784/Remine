<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# components

## Purpose
Shared, reusable UI building blocks for the Remine mobile-first app — layout shells (phone frame, screen, step layout), navigation chrome (bottom tab bar, step nav, mode switch bar), interaction primitives (pill button, progress dots, bottom sheet, suggestion sheet), and a live call screen. These are composed by page-level views; nothing here is a page/route itself. `icons/` holds the hand-authored SVG icon set used by navigation.

## Key Files
| File | Description |
|------|-------------|
| `PhoneFrame.tsx` | Renders a smartphone bezel around content at `sm`+ breakpoints (for desktop previewing); below `sm` it's an unstyled full-viewport container — the device itself is the frame. |
| `Screen.tsx` | Fills its container height exactly and never scrolls itself; only its content area scrolls, with an optional `footer` in normal flow (avoids `position: fixed` scroll bugs on mobile). |
| `BottomTabBar.tsx` | Role-aware (`parent`/`child`) bottom nav with 4 tabs (홈/오늘/추억/가족), using icons from `icons/NavIcons.tsx` and an `accentColor` prop for the active tab color. |
| `ModeBar.tsx` | Top bar showing current mode label/color with a link to `/switch-mode` (passed as a modal via `state.backgroundLocation`). |
| `StepNav.tsx` | Back/skip chevron nav used at the top of onboarding-style step flows. |
| `StepLayout.tsx` | Composes `Screen` + `StepNav` + `ProgressDots` + heading/subtitle into a standard step-flow page shell. |
| `ProgressDots.tsx` | Dot-based step progress indicator, active dot colored via `accentColor`. |
| `PillButton.tsx` | Full-width rounded primary action button, fixed at the bottom of a screen. |
| `BottomSheet.tsx` | Animated modal bottom sheet (fade + slide-up on mount), dismisses via backdrop click by navigating back (`navigate(-1)`). |
| `SuggestionSheet.tsx` | A `BottomSheet` preset with emoji/title/description and primary + "다음에 할게요" (dismiss) actions. |
| `CallScreen.tsx` | Full call UI (connecting → connected → ended) that calls `startCall`/`endCall` from `@/api/call`, tracks elapsed time, and auto-navigates back after hang-up. |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `icons/` | Icon components used by navigation (`NavIcons.tsx`: `HomeIcon`, `ClockIcon`, `MemoryIcon`, `FamilyIcon`, `BellIcon`), each a plain inline SVG accepting `className`/`style`. |

## For AI Agents

### Working In This Directory
- These *are* the shared components the project's "shared components only" rule refers to — build new UI by composing/extending these rather than restyling raw `<button>`/`<div>` in page code. Note this directory currently has no generic `Button`/`Card`/`Table` primitives yet (`PillButton` is the closest to a shared `Button`); if you add one, place it here.
- **No separate `navigation.ts` / `navIcons.ts` registration pair exists in this repo.** The two-place sidebar icon coupling described in the root CLAUDE.md is aspirational here — today, tab definitions live inline in `BottomTabBar.tsx` (`PARENT_TABS`/`CHILD_TABS` arrays) and import icons directly from `icons/NavIcons.tsx`. If tab/icon registration is later split into standalone files, update both in the same change and update this note.
- Colors are pulled from `@/theme`'s `COLORS` constant (`BottomTabBar.tsx`, `PillButton.tsx`, `SuggestionSheet.tsx`) or from Tailwind `remine-*` theme classes (e.g. `bg-remine-bg`, `text-remine-dark`) — never hardcoded hex values, per the single-color-source rule.
- Several components accept an `accentColor`/`color` prop (role- or context-driven) rather than hardcoding brand color, since the app is used by both `parent` and `child` roles with different accents.
- `CallScreen.tsx` is the only component here that talks to the network directly — it goes through `@/api/call.ts`, not raw `axios`/`fetch`, consistent with the domain-API-client rule.

### Testing Requirements
No test files exist under `src/components/` yet — no Vitest specs to run or extend.

### Common Patterns
- Props typed via a local `type <Component>Props = { ... }`, destructured directly in the function signature, default exported as a single function component per file.
- Sheets/modals (`BottomSheet`, `SuggestionSheet`) use `useNavigate()` + `navigate(-1)` for dismissal rather than local open/close state, since they're rendered as routes with a `backgroundLocation`.
- Layout components (`Screen`, `PhoneFrame`) carry a multi-line explanatory comment about *why* they avoid `position: fixed` — mobile browser address-bar/scroll quirks — worth preserving if touched.
- `CallScreen.tsx` guards against race conditions with `cancelled`/`endedRef` refs so an in-flight `startCall()` that resolves after unmount still gets cleaned up via `endCall`.

## Dependencies

### Internal
- `BottomTabBar.tsx` imports icons from `./icons/NavIcons.tsx` and `COLORS` from `@/theme`.
- `PillButton.tsx`, `SuggestionSheet.tsx` import `COLORS` from `@/theme`.
- `CallScreen.tsx` imports `startCall`/`endCall` from `@/api/call.ts`.
- `StepLayout.tsx` composes `Screen.tsx`, `StepNav.tsx`, `ProgressDots.tsx`.
- `SuggestionSheet.tsx` composes `BottomSheet.tsx`.
- `StepNav.tsx` imports a static asset, `@/assets/chevron.svg`.

### External
- `react-router-dom` (`useNavigate`, `NavLink`, `Link`, `useLocation`) — used throughout for navigation and active-tab/route state.
- No Chart.js or Zustand imports in this directory.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
