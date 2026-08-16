import { expect, test } from '@playwright/test'
import { bootDemoSession } from './helpers/app'
import { backendAs } from './helpers/backend'

test.describe('child — message flow', () => {
  test('sending a message appends it to the thread and persists it', async ({ page, request }) => {
    const backend = await backendAs(request, 'CHILD')
    const before = await backend.messageThread()

    await bootDemoSession(page)
    await page.goto('/child/family/message')

    // The existing history must render before we add to it.
    await expect(page.getByText('불러오는 중...')).toHaveCount(0)
    const bubbles = page.getByTestId('message-bubble')
    await expect(bubbles).toHaveCount(before.length)

    const body = `E2E 테스트 메시지 ${Date.now()}`
    await page.getByPlaceholder('메시지 입력...').fill(body)
    await page.getByRole('button', { name: '➤' }).click()

    // Rendered as the child's own outgoing bubble, and the input is cleared.
    const sent = bubbles.filter({ hasText: body })
    await expect(sent).toHaveCount(1)
    await expect(sent).toHaveAttribute('data-mine', 'true')
    await expect(page.getByPlaceholder('메시지 입력...')).toHaveValue('')
    await expect(bubbles).toHaveCount(before.length + 1)

    // It is a real write, not optimistic local state: the backend has it too.
    const after = await backend.messageThread()
    expect(after.map((m) => m.body), 'sent message must be persisted server-side').toContain(body)
    expect(after.length).toBe(before.length + 1)
  })

  test('a quick reply sends its own label as a message', async ({ page, request }) => {
    const backend = await backendAs(request, 'CHILD')
    const before = await backend.messageThread()

    await bootDemoSession(page)
    await page.goto('/child/family/message')
    await expect(page.getByTestId('message-bubble')).toHaveCount(before.length)

    const quickReply = page.getByRole('button', { name: '오늘도 화이팅이에요!' })
    await expect(quickReply).toBeVisible()
    await quickReply.click()

    await expect(page.getByTestId('message-bubble').filter({ hasText: '오늘도 화이팅이에요!' }).last()).toHaveAttribute(
      'data-mine',
      'true'
    )
    const after = await backend.messageThread()
    expect(after.length, 'quick reply must hit the backend too').toBe(before.length + 1)
    expect(after[0].body).toBe('오늘도 화이팅이에요!')
  })

  test('the message screen is reachable from the 가족 tab', async ({ page }) => {
    await bootDemoSession(page)
    await page.goto('/child/family')

    await page.getByRole('link', { name: /메시지 보내기/ }).click()
    await expect(page).toHaveURL(/\/child\/family\/message$/)
    // The header shows the counterpart's name once the paired profile lands.
    await expect(page.getByText('윤정아').first()).toBeVisible()
    await expect(page.getByPlaceholder('메시지 입력...')).toBeVisible()
  })
})
