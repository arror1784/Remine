// Plain `var(--color-x)` can't take a Tailwind alpha modifier (`bg-remine-blue/15`);
// only the explicit-alpha case is wrapped so base utilities emit the var unchanged.
const token = (name) => ({ opacityValue }) =>
  opacityValue === undefined || String(opacityValue).startsWith('var(')
    ? `var(--color-${name})`
    : `color-mix(in srgb, var(--color-${name}) ${Number(opacityValue) * 100}%, transparent)`

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        remine: {
          bg: token('bg'),
          text: token('text'),
          pink: token('pink'),
          blue: token('blue'),
          dark: token('dark'),
          muted: token('muted'),
          border: token('border'),
          highlight: token('highlight'),
          surface: token('surface'),
          subtle: token('subtle'),
          orange: token('orange'),
          surfaceAlt: token('surface-alt'),
          surfaceDark: token('surface-dark'),
          white: token('white'),
          surfaceSoft: token('surface-soft'),
          highlightBlue: token('highlight-blue'),
          nearBlack: token('near-black'),
          borderMuted: token('border-muted'),
          offline: token('offline'),
          gold: token('gold'),
          highlightOrange: token('highlight-orange'),
          borderSoft: token('border-soft'),
          deepPurple: token('deep-purple'),
          nearBlack2: token('near-black2'),
          surfaceSoft2: token('surface-soft2'),
          danger: token('danger'),
          teal: token('teal'),
          highlightPink: token('highlight-pink'),
          surfaceSoft3: token('surface-soft3'),
          dangerStrong: token('danger-strong'),
          surfaceSoft4: token('surface-soft4'),
        },
      },
    },
  },
  plugins: [],
}

