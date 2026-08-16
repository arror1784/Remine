import { expect, test } from '@playwright/test'
import { bootDemoSession } from './helpers/app'

test.describe('landing / demo login', () => {
  test('splash auto-logs in both roles and lands on a rendered parent home', async ({ page }) => {
    const failedRequests: string[] = []
    page.on('response', (response) => {
      if (response.url().includes('/api/v1/auth/demo-login') && !response.ok()) {
        failedRequests.push(`${response.status()} ${response.url()}`)
      }
    })

    await bootDemoSession(page)

    expect(failedRequests, 'demo-login must not fail').toEqual([])
    await expect(page).toHaveURL(/\/parent\/home$/)

    // Both role sessions get persisted, which is what makes the /child/* screens
    // reachable without a second login.
    const sessions = await page.evaluate(() => {
      const raw = window.localStorage.getItem('remine-auth')
      return raw ? JSON.parse(raw).state.sessions : null
    })
    expect(sessions?.parent?.accessToken, 'parent session token').toBeTruthy()
    expect(sessions?.child?.accessToken, 'child session token').toBeTruthy()

    // Home must be actually rendered, not stuck on the splash logo or a spinner.
    await expect(page.getByText('부모님 모드', { exact: false })).toBeVisible()
    await expect(page.getByText('불러오는 중...')).toHaveCount(0)
    await expect(page.locator('img[alt="Remine"]')).toHaveCount(0)

    const bodyText = await page.locator('body').innerText()
    expect(bodyText.trim().length, 'home screen should render real copy').toBeGreaterThan(100)
  })

  test('the bottom tab bar navigates between the parent screens', async ({ page }) => {
    await bootDemoSession(page)

    await page.getByRole('link', { name: '오늘' }).click()
    await expect(page).toHaveURL(/\/parent\/today$/)
    await expect(page.getByRole('heading', { name: '오늘의 분석' })).toBeVisible()

    await page.getByRole('link', { name: '추억' }).click()
    await expect(page).toHaveURL(/\/parent\/memories$/)
    await expect(page.getByRole('heading', { name: '추억 갤러리' })).toBeVisible()
  })
})
