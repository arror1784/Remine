import { expect, test } from '@playwright/test'
import { bootDemoSession, expectImagesLoaded } from './helpers/app'
import { backendAs } from './helpers/backend'

function digitsOf(text: string): number {
  return Number(text.replace(/[^\d]/g, ''))
}

test.describe('child — 가족 tab', () => {
  test('summary stats, recent chat and shared photos all come from the backend', async ({ page, request }) => {
    const backend = await backendAs(request, 'CHILD')
    const [summary, thread, photos] = await Promise.all([
      backend.familySummary(),
      backend.messageThread(),
      backend.memoryGallery(),
    ])

    await bootDemoSession(page)
    await page.goto('/child/family')

    await expect(page.getByRole('heading', { name: '가족' })).toBeVisible()
    await expect(page.getByText('불러오는 중...')).toHaveCount(0)
    await expect(page.getByText('아직 연결된 부모님이 없어요')).toHaveCount(0)
    await expect(page.getByText('불러오지 못했어요')).toHaveCount(0)

    // 1. Family summary stats — these were hardcoded mock numbers before.
    const stats = page.getByTestId('family-stats')
    await expect(stats).toBeVisible()
    expect(digitsOf(await stats.getByTestId('stat-messages').innerText())).toBe(summary.messageCount)
    expect(digitsOf(await stats.getByTestId('stat-photos').innerText())).toBe(summary.sharedPhotoCount)
    expect(digitsOf(await stats.getByTestId('stat-calls').innerText())).toBe(summary.callCount)
    expect(summary.messageCount, 'seeded pair should have real chat history').toBeGreaterThan(0)

    // 2. Recent chat — the screen shows the two most recent thread entries.
    // `getThread` reverses the backend's newest-first page into chronological
    // order, so the tail of that reversed list is what should be on screen.
    const chronological = thread.slice().reverse()
    const expectedRecent = chronological.slice(-2).map((m) => m.body)
    const renderedChat = await page.getByTestId('recent-chat-body').allInnerTexts()
    expect(renderedChat.map((t) => t.trim()), 'recent chat must be the newest real messages').toEqual(expectedRecent)

    // 3. Shared photos — first two of the real gallery, and they must decode.
    const sharedCards = page.getByTestId('shared-photo')
    const expectedShared = photos.slice(0, 2)
    await expect(sharedCards).toHaveCount(expectedShared.length)
    for (const [index, photo] of expectedShared.entries()) {
      await expect(sharedCards.nth(index).locator('img')).toHaveAttribute('alt', photo.title)
    }
    await expectImagesLoaded(sharedCards.locator('img'), expectedShared.length)
  })

  test('the paired parent is resolved from the child session, not the parent one', async ({ page, request }) => {
    const backend = await backendAs(request, 'CHILD')
    const summary = await backend.familySummary()
    expect(summary).toBeTruthy()

    await bootDemoSession(page)
    await page.goto('/child/family')

    await expect(page.getByText('자녀 모드', { exact: false })).toBeVisible()
    // Entering /child/* by URL while `activeRole` is still the persisted
    // 'parent' relies on App's path→role effect landing before the screen's
    // own fetch is signed. If that ordering ever breaks, this page renders the
    // child's own name here instead of the parent's, so assert the parent's.
    await expect(page.getByText('함께하는 가족')).toBeVisible()
    await expect(page.getByText('윤정아님')).toBeVisible()

    await expect(page.getByRole('link', { name: /메시지 보내기/ })).toBeVisible()
    await expect(page.getByRole('link', { name: /전화하기/ })).toBeVisible()
  })
})
