/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        remine: {
          bg: 'var(--color-bg)',
          text: 'var(--color-text)',
          pink: 'var(--color-pink)',
          blue: 'var(--color-blue)',
          dark: 'var(--color-dark)',
        },
      },
    },
  },
  plugins: [],
}

