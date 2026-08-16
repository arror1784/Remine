import { expect, test } from '@playwright/test'
import { bootDemoSession, expectImagesLoaded } from './helpers/app'
import { backendAs } from './helpers/backend'

function yearMonth(iso: string): string {
  const d = new Date(iso)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월`
}

test.describe('parent — 추억 갤러리', () => {
  test('renders one card per backend photo, with its real title and date', async ({ page, request }) => {
    const backend = await backendAs(request, 'PARENT')
    const photos = await backend.memoryGallery()
    expect(photos.length, 'seeded parent must own at least one memory photo').toBeGreaterThan(0)

    await bootDemoSession(page)
    await page.goto('/parent/memories')

    await expect(page.getByRole('heading', { name: '추억 갤러리' })).toBeVisible()
    await expect(page.getByText('불러오는 중...')).toHaveCount(0)
    await expect(page.getByText('불러오지 못했어요')).toHaveCount(0)
    await expect(page.getByText('아직 등록된 추억 사진이 없어요')).toHaveCount(0)

    const cards = page.getByTestId('memory-card')
    await expect(cards).toHaveCount(photos.length)

    for (const [index, photo] of photos.entries()) {
      const card = cards.nth(index)
      await expect(card.getByTestId('memory-title')).toHaveText(photo.title)
      await expect(card.getByTestId('memory-date')).toHaveText(yearMonth(photo.createdAt))
    }
  })

  test('every photo actually decodes — a broken photoUrl must fail here', async ({ page, request }) => {
    const backend = await backendAs(request, 'PARENT')
    const photos = await backend.memoryGallery()

    // A 404 on a photo still renders an <img> with alt text, so the only honest
    // check is whether the browser decoded pixels for it.
    const brokenResponses: string[] = []
    page.on('response', (response) => {
      if (response.request().resourceType() === 'image' && !response.ok()) {
        brokenResponses.push(`${response.status()} ${response.url()}`)
      }
    })

    await bootDemoSession(page)
    await page.goto('/parent/memories')

    await expectImagesLoaded(page.getByTestId('memory-card').locator('img'), photos.length)
    expect(brokenResponses, 'no memory photo request may 404').toEqual([])
  })
})
