<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# src

## Purpose
Application source root. `main.tsx` mounts the app and `App.tsx` owns the single `<Routes>` tree (see `pages/AGENTS.md`); everything else is organized by concern rather than by route — network clients (`api/`), global state (`store/`), reusable UI (`components/`), route screens (`pages/`), and static imports (`assets/`) — with `theme.ts` as the single source of color tokens.

## Key Files
| File | Description |
|------|-------------|
| `main.tsx` | React root render, mounts `<App />` |
| `App.tsx` | Top-level `<Routes>` tree for both `parent/*` and `child/*`, plus the background-route overlay pattern for bottom sheets |
| `theme.ts` | Single color source — all colors defined once here as CSS-variable-backed tokens; components must not hardcode hex values |
| `index.css` | Tailwind entry + global styles |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `api/` | Domain-scoped Axios clients, one per backend domain, all unwrapping the shared `{ data, error }` envelope (see `api/AGENTS.md`) |
| `components/` | Shared, reusable UI building blocks (layout shells, nav chrome, primitives) plus `components/icons/` (see `components/AGENTS.md`) |
| `pages/` | Route screens for `parent/`, `child/`, and `onboarding/` (see `pages/AGENTS.md`, `pages/parent/AGENTS.md`, `pages/child/AGENTS.md`) |
| `store/` | Global Zustand stores (see `store/AGENTS.md`) |
| `assets/` | Static images/SVGs imported directly by components (icons, onboarding art, sample memory photos) |

## For AI Agents

### Working In This Directory
Keep new code inside the existing per-concern split rather than co-locating everything under `pages/` — a new backend endpoint gets a function in the matching `api/<domain>.ts` file (or a new file if the domain is new), not an inline `axios` call in a page component. A new piece of shared UI goes in `components/`, not duplicated per-page. Cross-cutting UI state that must survive a page staying mounted underneath a route overlay (see `pages/AGENTS.md` for the background-route gotcha) belongs in `store/`, not local `useState`.

### Testing Requirements
No test tooling is installed under `src/` yet (see `Remine_frontend/AGENTS.md`).

### Common Patterns
Backend contract is camelCase end-to-end; always preserve the response `id` field so CRUD stays in sync with the backend. Bulk list saves go through a single `PATCH .../sync` call built in the relevant `api/<domain>.ts` file, never one call per row.

## Dependencies

### Internal
`pages/` depends on `api/`, `store/`, `components/`, and `theme.ts`; `api/` depends on `store/auth.ts` (for the active session token) via `api/http.ts`'s request interceptor.

### External
See `Remine_frontend/AGENTS.md` for the full package list; the files directly under `src/` mainly touch `react`, `react-router-dom`, and Tailwind's generated classes.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
