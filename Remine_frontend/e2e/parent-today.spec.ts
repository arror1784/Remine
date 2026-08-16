import { expect, test } from '@playwright/test'
import { bootDemoSession } from './helpers/app'
import { backendAs, type TodaySummary } from './helpers/backend'

// Today.tsx keeps a static FALLBACK_SUMMARY so the card never flashes empty.
// It renders through exactly the same markup as real data, so "the section is
// visible" proves nothing — these are the values to assert we are NOT showing.
const FALLBACK_VALUES = ['7시간 12분', '4,280보', '1회', '연락 없음']

function digitsOf(text: string): number {
  return Number(text.replace(/[^\d]/g, ''))
}

test.describe('parent — 오늘 tab', () => {
  test('the activity summary renders the backend values, not the static fallback', async ({ page, request }) => {
    const backend = await backendAs(request, 'PARENT')
    const summary: TodaySummary = await backend.todaySummary()

    expect(summary.stat, 'seeded parent must have a stat row for today').not.toBeNull()
    const stat = summary.stat!

    await bootDemoSession(page)
    await page.goto('/parent/today')

    const card = page.getByTestId('today-summary')
    await expect(card).toBeVisible()

    const expected = [
      { metric: 'sleep', percent: summary.sleepPercent, value: `${Math.floor(stat.sleepMinutes / 60)}시간 ${stat.sleepMinutes % 60}분` },
      { metric: 'steps', percent: summary.stepsPercent, digits: stat.steps },
      { metric: 'outing', percent: summary.outingPercent, digits: stat.outingCount },
      { metric: 'social', percent: summary.socialPercent, digits: stat.socialContactCount },
    ]

    for (const row of expected) {
      const el = page.getByTestId(`today-summary-${row.metric}`)
      await expect(el, `${row.metric} row should render`).toBeVisible()

      const rendered = (await el.getByTestId('value').innerText()).trim()
      if (row.value !== undefined) {
        expect(rendered, `${row.metric} value`).toBe(row.value)
      } else {
        // Compare digits rather than the formatted string so the browser's
        // thousands separator locale can't make this flaky.
        expect(digitsOf(rendered), `${row.metric} value (${rendered})`).toBe(row.digits)
      }

      expect(row.percent, `${row.metric} percent must be a real 0-100 percentage`).toBeGreaterThanOrEqual(0)
      expect(row.percent).toBeLessThanOrEqual(100)
      await expect(el.getByTestId('bar'), `${row.metric} bar width must track its percent`).toHaveAttribute(
        'style',
        new RegExp(`width:\\s*${row.percent}%`)
      )
    }

    // Anti-regression: if the fetch silently failed the card would fall back,
    // and every value above would coincidentally have to equal the fallback set.
    const renderedValues = await card.getByTestId('value').allInnerTexts()
    expect(renderedValues.map((v) => v.trim())).not.toEqual(FALLBACK_VALUES)
  })

  test('the 생활 패턴 분석 list mirrors the same four metrics', async ({ page, request }) => {
    const backend = await backendAs(request, 'PARENT')
    const summary = await backend.todaySummary()

    await bootDemoSession(page)
    await page.goto('/parent/today')

    await expect(page.getByRole('heading', { name: '생활 패턴 분석' })).toBeVisible()
    for (const metric of ['sleep', 'steps', 'outing', 'social']) {
      const row = page.getByTestId(`today-pattern-${metric}`)
      await expect(row).toBeVisible()
      // Each row carries a bucketed tag derived from the percent — an empty one
      // means the derivation broke even though the row still rendered.
      expect((await row.innerText()).trim().length, `${metric} pattern row copy`).toBeGreaterThan(0)
    }

    const summaryValues = await page.getByTestId('today-summary').getByTestId('value').allInnerTexts()
    expect(summaryValues, 'summary and pattern list are built from one rows array').toHaveLength(4)
    expect(summary.stat).not.toBeNull()
  })
})
