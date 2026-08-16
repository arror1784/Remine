import { defineConfig, devices } from '@playwright/test'

export const BASE_URL = process.env.E2E_BASE_URL ?? 'http://localhost:5173'
export const API_BASE_URL = process.env.E2E_API_BASE_URL ?? 'http://localhost:8080'

export default defineConfig({
  testDir: './e2e',
  globalSetup: './e2e/global-setup.ts',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: process.env.CI ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: BASE_URL,
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    // The app is a phone-frame layout; a desktop-width viewport still renders it
    // but a phone viewport is what every screen was actually designed against.
    viewport: { width: 430, height: 932 },
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: `yarn dev --port ${new URL(BASE_URL).port} --strictPort`,
    url: BASE_URL,
    reuseExistingServer: true,
    timeout: 60_000,
    stdout: 'ignore',
    stderr: 'pipe',
  },
})
