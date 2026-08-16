<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-08-17 | Updated: 2026-08-17 -->

# Remine_frontend

## Purpose
React 18 + TypeScript mobile-web frontend for Remine, built with Vite. The whole app renders inside a phone-frame shell (`src/components/PhoneFrame.tsx`) and is a single-page app with two parallel role experiences — `parent/*` routes (부모님, older-adult) and `child/*` routes (자녀, caregiver) — plus shared onboarding/login/switch-mode screens, all wired through one flat `<Routes>` tree in `src/App.tsx`.

## Key Files
| File | Description |
|------|-------------|
| `package.json` | Scripts: `dev` (vite), `build` (`tsc -b && vite build` — the build **is** the type check), `lint` (oxlint), `preview` |
| `vite.config.ts` | Vite build config |
| `tailwind.config.*` / `postcss.config.*` | Tailwind CSS setup |
| `tsconfig*.json` | TypeScript project config |
| `index.html` | Vite entry HTML |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/` | All application source (see `src/AGENTS.md`) |
| `public/` | Static assets served as-is |

## For AI Agents

### Working In This Directory
Package manager is **Yarn Classic** (per root `CLAUDE.md`) — use `yarn`, not `npm`, for installs. `yarn build` (`tsc -b && vite build`) is the authoritative type-check for this project; run it after any non-trivial change instead of assuming `tsc` alone is enough, since Vite-specific import/asset handling can fail even when raw `tsc` would pass. Two conventions from root `CLAUDE.md` are worth double-checking against current reality before relying on them: it describes Chart.js for dashboard charts and Vitest for unit tests, but neither package appears in `package.json` as of this writing — treat those as target/aspirational, not implemented, until verified again. All colors must come from the single CSS-variable theme file (`src/theme.ts` / `src/index.css`) — no hardcoded hex or `text-[#...]` in components.

### Testing Requirements
No test runner is currently installed (no Vitest, no `*.test.*`/`*.spec.*` files anywhere under `src/`). If adding tests, install Vitest first per the root `CLAUDE.md` target stack rather than reaching for a different runner.

### Common Patterns
Domain API clients only — components never call `axios`/`fetch` directly, always through `src/api/<domain>.ts`. Global state is Zustand (`src/store/`). Routing is `react-router-dom`, including a "background route" overlay pattern for bottom sheets/reminders (documented in `src/pages/AGENTS.md`) where a page can stay mounted underneath an overlay and must use shared store state, not local `useState`, to reflect what happened in the overlay.

## Dependencies

### Internal
N/A — top of the frontend subtree.

### External
`react` / `react-dom` 18, `react-router-dom` 7, `axios`, `zustand`, `tailwindcss`, `typescript`, `vite` 8, `oxlint` (linter, not ESLint).

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
