import { expect, type Locator, type Page } from '@playwright/test'

/**
 * Splash auto-runs `demoLogin` for both roles and persists them under the
 * `remine-auth` localStorage key, so every spec starts here to get a session
 * rather than seeding tokens by hand.
 */
export async function bootDemoSession(page: Page) {
  await page.goto('/')
  // Splash falls through to /onboarding whenever demo-login throws, so wait for
  // either outcome and report the real cause rather than timing out blind.
  await page.waitForURL(/\/(parent\/home|onboarding)$/, { timeout: 20_000 })
  if (new URL(page.url()).pathname === '/onboarding') {
    throw new Error(
      'Splash could not complete demo-login and fell back to /onboarding. Usually either the ' +
        'backend is down, or the dev server is on a port the backend does not allow: CorsConfig ' +
        `whitelists only http://localhost:5173 and http://localhost:5174, and this run used ${new URL(page.url()).origin}.`
    )
  }
}

/** Fails unless the browser actually decoded pixels for every matched <img>. */
export async function expectImagesLoaded(images: Locator, expectedCount: number) {
  await expect(images).toHaveCount(expectedCount)
  for (let i = 0; i < expectedCount; i++) {
    const image = images.nth(i)
    await expect(image).toBeVisible()
    const src = await image.getAttribute('src')
    await expect
      .poll(() => image.evaluate((node: HTMLImageElement) => node.naturalWidth), {
        message: `<img src="${src}"> never decoded — the URL is broken or 404s`,
        timeout: 10_000,
      })
      .toBeGreaterThan(0)
  }
}
