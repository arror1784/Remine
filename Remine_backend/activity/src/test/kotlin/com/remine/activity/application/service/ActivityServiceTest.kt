package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetCheerMessageSuggestionsQuery
import com.remine.activity.application.port.inbound.GetChecklistQuery
import com.remine.activity.application.port.inbound.GetTodaySummaryQuery
import com.remine.activity.application.port.inbound.GetWeeklyPatternQuery
import com.remine.activity.application.port.inbound.RecordDailyActivityCommand
import com.remine.activity.application.port.inbound.SendCheerCommand
import com.remine.activity.application.port.inbound.SyncDailyActivityCommand
import com.remine.activity.application.port.inbound.ToggleChecklistItemCommand
import com.remine.activity.application.port.inbound.UpdateDailyActivityCommand
import com.remine.activity.application.port.outbound.ActivityCheerRepositoryPort
import com.remine.activity.application.port.outbound.ActivityChecklistItemRepositoryPort
import com.remine.activity.application.port.outbound.CheerMessageGeneratorPort
import com.remine.activity.application.port.outbound.DailyActivityRecommendationRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.ActivityCheer
import com.remine.activity.domain.ActivityChecklistItem
import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.activity.domain.DailyActivityStat
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ActivityServiceTest {

    private class InMemoryDailyActivityStatRepository : DailyActivityStatRepositoryPort {
        val store = mutableMapOf<UUID, DailyActivityStat>()

        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStat? {
            return store.values.firstOrNull { it.userId == userId && it.statDate == statDate && it.deletedAt == null }
        }

        override fun findByUserIdAndStatDateIn(
            userId: UUID,
            statDates: Collection<LocalDate>,
        ): List<DailyActivityStat> {
            return store.values.filter { it.userId == userId && it.statDate in statDates && it.deletedAt == null }
        }

        override fun findByUserIdAndStatDateBetween(
            userId: UUID,
            startDate: LocalDate,
            endDate: LocalDate,
        ): List<DailyActivityStat> {
            return store.values.filter {
                it.userId == userId && !it.statDate.isBefore(startDate) && !it.statDate.isAfter(endDate) && it.deletedAt == null
            }.sortedBy { it.statDate }
        }

        override fun save(stat: DailyActivityStat): DailyActivityStat {
            store[stat.id] = stat
            return stat
        }

        override fun saveAll(stats: Collection<DailyActivityStat>): List<DailyActivityStat> {
            stats.forEach { store[it.id] = it }
            return stats.toList()
        }
    }

    private class InMemoryDailyActivityRecommendationRepository : DailyActivityRecommendationRepositoryPort {
        val store = mutableMapOf<UUID, DailyActivityRecommendation>()

        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendation? {
            return store.values.firstOrNull { it.userId == userId && it.statDate == statDate }
        }

        override fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation {
            store[recommendation.id] = recommendation
            return recommendation
        }

        override fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate) {
            store.values.removeIf { it.userId == userId && it.statDate == statDate }
        }
    }

    private class MockCheerMessageGeneratorPort : CheerMessageGeneratorPort {
        var lastItemType: String? = null
        var returnSuggestions = listOf("메시지1", "메시지2", "메시지3")

        override fun generateSuggestions(
            itemType: String,
            stat: DailyActivityStat?,
            sleepPercent: Int,
            stepsPercent: Int,
            outingPercent: Int,
            socialPercent: Int,
        ): List<String> {
            lastItemType = itemType
            return returnSuggestions
        }
    }

    private class InMemoryChecklistItemRepository : ActivityChecklistItemRepositoryPort {
        val store = mutableMapOf<UUID, ActivityChecklistItem>()

        override fun findById(id: UUID): ActivityChecklistItem? {
            return store[id]?.takeIf { it.deletedAt == null }
        }

        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): List<ActivityChecklistItem> {
            return store.values.filter { it.userId == userId && it.statDate == statDate && it.deletedAt == null }
        }

        override fun save(item: ActivityChecklistItem): ActivityChecklistItem {
            store[item.id] = item
            return item
        }

        override fun saveAll(items: Collection<ActivityChecklistItem>): List<ActivityChecklistItem> {
            items.forEach { store[it.id] = it }
            return items.toList()
        }
    }

    private class InMemoryCheerRepository : ActivityCheerRepositoryPort {
        val store = mutableMapOf<UUID, ActivityCheer>()

        override fun findByChecklistItemIdAndSenderUserId(
            checklistItemId: UUID,
            senderUserId: UUID,
        ): List<ActivityCheer> {
            return store.values.filter {
                it.checklistItemId == checklistItemId && it.senderUserId == senderUserId && it.deletedAt == null
            }
        }

        override fun findByChecklistItemIdAndSenderUserIdAndSentAtBetween(
            checklistItemId: UUID,
            senderUserId: UUID,
            startOfDay: Instant,
            endOfDay: Instant,
        ): List<ActivityCheer> {
            return store.values.filter {
                it.checklistItemId == checklistItemId &&
                    it.senderUserId == senderUserId &&
                    !it.sentAt.isBefore(startOfDay) &&
                    !it.sentAt.isAfter(endOfDay) &&
                    it.deletedAt == null
            }
        }

        override fun save(cheer: ActivityCheer): ActivityCheer {
            store[cheer.id] = cheer
            return cheer
        }
    }

    @Test
    fun `record daily activity creates new stat and rejects duplicate date`() {
        val repo = InMemoryDailyActivityStatRepository()
        val service = DailyActivityService(repo, InMemoryDailyActivityRecommendationRepository())
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val out = service.handle(
            RecordDailyActivityCommand.In(
                userId = userId,
                statDate = today,
                sleepMinutes = 450,
                steps = 6000,
                outingCount = 2,
                socialContactCount = 1,
            )
        )

        assertEquals(450, out.entity.sleepMinutes)
        assertEquals(6000, out.entity.steps)
        assertEquals(2, out.entity.outingCount)
        assertEquals(1, out.entity.socialContactCount)

        assertThrows<InvalidRequestException> {
            service.handle(
                RecordDailyActivityCommand.In(
                    userId = userId,
                    statDate = today,
                    sleepMinutes = 300,
                    steps = 1000,
                    outingCount = 0,
                    socialContactCount = 0,
                )
            )
        }
    }

    @Test
    fun `update daily activity updates existing stat partially and drops the cached recommendation`() {
        val repo = InMemoryDailyActivityStatRepository()
        val recRepo = InMemoryDailyActivityRecommendationRepository()
        val service = DailyActivityService(repo, recRepo)
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        service.handle(
            RecordDailyActivityCommand.In(
                userId = userId,
                statDate = today,
                sleepMinutes = 400,
                steps = 5000,
                outingCount = 1,
                socialContactCount = 1,
            )
        )
        // Stand in for a recommendation generated against the stat above, before the update below.
        recRepo.save(
            DailyActivityRecommendation(
                userId = userId,
                statDate = today,
                parentMessage = "업데이트 전 통계로 만든 추천",
                childMessage = "업데이트 전 통계로 만든 추천",
                actionType = DailyActivityRecommendationActionType.NONE,
            )
        )

        val updated = service.handle(
            UpdateDailyActivityCommand.In(
                userId = userId,
                statDate = today,
                sleepMinutes = null,
                steps = 8500,
                outingCount = null,
                socialContactCount = 3,
            )
        )

        assertEquals(400, updated.entity.sleepMinutes)
        assertEquals(8500, updated.entity.steps)
        assertEquals(1, updated.entity.outingCount)
        assertEquals(3, updated.entity.socialContactCount)
        assertNull(recRepo.findByUserIdAndStatDate(userId, today))
    }

    @Test
    fun `sync daily activity performs bulk upsert`() {
        val repo = InMemoryDailyActivityStatRepository()
        val service = DailyActivityService(repo, InMemoryDailyActivityRecommendationRepository())
        val userId = UUID.randomUUID()
        val date1 = LocalDate.of(2026, 8, 1)
        val date2 = LocalDate.of(2026, 8, 2)

        val syncOut1 = service.handle(
            SyncDailyActivityCommand.In(
                userId = userId,
                entries = listOf(
                    SyncDailyActivityCommand.In.Entry(date1, 400, 3000, 1, 1),
                    SyncDailyActivityCommand.In.Entry(date2, 450, 7000, 2, 2),
                )
            )
        )
        assertEquals(2, syncOut1.savedCount)

        val syncOut2 = service.handle(
            SyncDailyActivityCommand.In(
                userId = userId,
                entries = listOf(
                    SyncDailyActivityCommand.In.Entry(date1, 500, 4000, 1, 1),
                )
            )
        )
        assertEquals(1, syncOut2.savedCount)
        val fetched = repo.findByUserIdAndStatDate(userId, date1)
        assertEquals(500, fetched?.sleepMinutes)
        assertEquals(4000, fetched?.steps)
    }

    @Test
    fun `today summary returns computed percents capped at 100`() {
        val repo = InMemoryDailyActivityStatRepository()
        val service = DailyActivityService(repo, InMemoryDailyActivityRecommendationRepository())
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val emptySummary = service.handle(GetTodaySummaryQuery.In(userId))
        assertNull(emptySummary.stat)
        assertEquals(0, emptySummary.stepsPercent)

        service.handle(
            RecordDailyActivityCommand.In(
                userId = userId,
                statDate = today,
                sleepMinutes = 480, // goal 480 -> 100%
                steps = 12000, // goal 8000 -> 150% -> capped at 100%
                outingCount = 1, // goal 1 -> 100%
                socialContactCount = 0, // goal 1 -> 0%
            )
        )

        val summary = service.handle(GetTodaySummaryQuery.In(userId))
        assertNotNull(summary.stat)
        assertEquals(100, summary.sleepPercent)
        assertEquals(100, summary.stepsPercent)
        assertEquals(100, summary.outingPercent)
        assertEquals(0, summary.socialPercent)
    }

    @Test
    fun `weekly pattern returns 7 days chronologically`() {
        val repo = InMemoryDailyActivityStatRepository()
        val service = DailyActivityService(repo, InMemoryDailyActivityRecommendationRepository())
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        service.handle(
            RecordDailyActivityCommand.In(
                userId = userId,
                statDate = today.minusDays(2),
                sleepMinutes = 400,
                steps = 5000,
                outingCount = 1,
                socialContactCount = 1,
            )
        )

        val weekly = service.handle(GetWeeklyPatternQuery.In(userId))
        assertEquals(7, weekly.days.size)
        assertEquals(today.minusDays(6), weekly.days[0].statDate)
        assertEquals(today, weekly.days[6].statDate)
        assertTrue(weekly.days[6].isToday)
        assertEquals(5000, weekly.days[4].steps)
    }

    @Test
    fun `checklist find-or-create creates 4 items, toggles done, cheers idempotently`() {
        val checklistRepo = InMemoryChecklistItemRepository()
        val cheerRepo = InMemoryCheerRepository()
        val service = ActivityChecklistService(checklistRepo, cheerRepo, InMemoryDailyActivityStatRepository(), MockCheerMessageGeneratorPort())
        val userId = UUID.randomUUID()
        val senderId = UUID.randomUUID()
        val today = LocalDate.now()

        val checklist = service.handle(GetChecklistQuery.In(userId, today))
        assertEquals(4, checklist.items.size)
        assertEquals(listOf("SLEEP", "BREAKFAST", "WALK", "QUIZ"), checklist.items.map { it.type })

        val firstItem = checklist.items[0]
        val toggled = service.handle(ToggleChecklistItemCommand.In(firstItem.id, true, userId))
        assertTrue(toggled.entity.done)
        assertNotNull(toggled.entity.completedAt)

        val cheer1 = service.handle(SendCheerCommand.In(firstItem.id, senderId, userId))
        assertNotNull(cheer1.entity)

        val cheer2 = service.handle(SendCheerCommand.In(firstItem.id, senderId, userId))
        assertNull(cheer2.entity) // idempotent on same day
    }

    @Test
    fun `cheer message suggestions pass the checklist item's type and stat percents to the AI generator`() {
        val checklistRepo = InMemoryChecklistItemRepository()
        val cheerRepo = InMemoryCheerRepository()
        val statRepo = InMemoryDailyActivityStatRepository()
        val generator = MockCheerMessageGeneratorPort()
        val service = ActivityChecklistService(checklistRepo, cheerRepo, statRepo, generator)
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val checklist = service.handle(GetChecklistQuery.In(userId, today))
        val walkItem = checklist.items.first { it.type == "WALK" }
        statRepo.save(
            DailyActivityStat(
                userId = userId,
                statDate = today,
                sleepMinutes = 480,
                steps = 4000,
                outingCount = 0,
                socialContactCount = 1,
                sleepGoalMinutes = 480,
                stepsGoal = 8000,
                outingGoal = 1,
                socialGoal = 1,
            ),
        )

        val out = service.handle(GetCheerMessageSuggestionsQuery.In(walkItem.id, userId))

        assertEquals(listOf("메시지1", "메시지2", "메시지3"), out.suggestions)
        assertEquals("WALK", generator.lastItemType)
    }

    @Test
    fun `cheer message suggestions are rejected for another family's checklist item`() {
        val checklistRepo = InMemoryChecklistItemRepository()
        val cheerRepo = InMemoryCheerRepository()
        val service = ActivityChecklistService(checklistRepo, cheerRepo, InMemoryDailyActivityStatRepository(), MockCheerMessageGeneratorPort())
        val parentId = UUID.randomUUID()
        val outsiderId = UUID.randomUUID()
        val item = service.handle(GetChecklistQuery.In(parentId, LocalDate.now())).items.first()

        assertThrows<ForbiddenException> {
            service.handle(GetCheerMessageSuggestionsQuery.In(item.id, outsiderId))
        }
    }

    @Test
    fun `checklist toggle and cheer are rejected for another family's checklist item`() {
        val checklistRepo = InMemoryChecklistItemRepository()
        val cheerRepo = InMemoryCheerRepository()
        val service = ActivityChecklistService(checklistRepo, cheerRepo, InMemoryDailyActivityStatRepository(), MockCheerMessageGeneratorPort())
        val parentId = UUID.randomUUID()
        val outsiderId = UUID.randomUUID()

        val item = service.handle(GetChecklistQuery.In(parentId, LocalDate.now())).items.first()

        assertThrows<ForbiddenException> {
            service.handle(ToggleChecklistItemCommand.In(item.id, true, outsiderId))
        }
        assertThrows<ForbiddenException> {
            service.handle(SendCheerCommand.In(item.id, outsiderId, outsiderId))
        }

        assertFalse(checklistRepo.findById(item.id)!!.done)
        assertTrue(cheerRepo.store.isEmpty())
    }
}
