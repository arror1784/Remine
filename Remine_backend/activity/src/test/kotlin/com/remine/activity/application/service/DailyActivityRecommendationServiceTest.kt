package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetDailyActivityRecommendationQuery
import com.remine.activity.application.port.outbound.ActivityRecommendationGeneratorPort
import com.remine.activity.application.port.outbound.DailyActivityRecommendationRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.activity.domain.DailyActivityStat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDate
import java.util.UUID

class DailyActivityRecommendationServiceTest {

    private class InMemoryDailyActivityStatRepository : DailyActivityStatRepositoryPort {
        val store = mutableMapOf<UUID, DailyActivityStat>()

        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStat? {
            return store.values.firstOrNull { it.userId == userId && it.statDate == statDate && it.deletedAt == null }
        }

        override fun findByUserIdAndStatDateIn(userId: UUID, statDates: Collection<LocalDate>): List<DailyActivityStat> {
            return store.values.filter { it.userId == userId && it.statDate in statDates && it.deletedAt == null }
        }

        override fun findByUserIdAndStatDateBetween(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<DailyActivityStat> {
            return store.values.filter {
                it.userId == userId && !it.statDate.isBefore(startDate) && !it.statDate.isAfter(endDate) && it.deletedAt == null
            }
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
            return store.values.firstOrNull { it.userId == userId && it.statDate == statDate && it.deletedAt == null }
        }

        override fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation {
            store[recommendation.id] = recommendation
            return recommendation
        }

        override fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate) {
            store.values.removeIf { it.userId == userId && it.statDate == statDate }
        }
    }

    /** Simulates a concurrent request that already saved [winner] under our own save's feet. */
    private class RaceSimulatingRecommendationRepository(
        private val winner: DailyActivityRecommendation,
    ) : DailyActivityRecommendationRepositoryPort {
        var findCallCount = 0

        override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendation? {
            findCallCount++
            // Miss on the pre-generation cache check, then reveal the concurrent winner once
            // the service re-queries after catching our own save's constraint violation.
            return if (findCallCount == 1) null else winner
        }

        override fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation {
            throw DataIntegrityViolationException("duplicate key value violates unique constraint")
        }

        override fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate) = Unit
    }

    private class MockActivityRecommendationGenerator : ActivityRecommendationGeneratorPort {
        var callCount = 0
        var lastStat: DailyActivityStat? = null
        var lastSleepPercent: Int = 0
        var lastStepsPercent: Int = 0
        var lastOutingPercent: Int = 0
        var lastSocialPercent: Int = 0

        var returnParentMessage = "오늘 오후 산책 어떠세요?"
        var returnChildMessage = "어머니가 오늘 아직 외출을 못 하셨어요."
        var returnActionType = DailyActivityRecommendationActionType.WALK

        override fun generateRecommendation(
            stat: DailyActivityStat,
            sleepPercent: Int,
            stepsPercent: Int,
            outingPercent: Int,
            socialPercent: Int,
        ): ActivityRecommendationGeneratorPort.GeneratedRecommendation {
            callCount++
            lastStat = stat
            lastSleepPercent = sleepPercent
            lastStepsPercent = stepsPercent
            lastOutingPercent = outingPercent
            lastSocialPercent = socialPercent

            return ActivityRecommendationGeneratorPort.GeneratedRecommendation(
                parentMessage = returnParentMessage,
                childMessage = returnChildMessage,
                actionType = returnActionType,
            )
        }
    }

    @Test
    fun `returns cached recommendation without querying stats or calling AI`() {
        val statRepo = InMemoryDailyActivityStatRepository()
        val recRepo = InMemoryDailyActivityRecommendationRepository()
        val aiGenerator = MockActivityRecommendationGenerator()
        val service = DailyActivityRecommendationService(statRepo, recRepo, aiGenerator)

        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val cached = DailyActivityRecommendation(
            userId = userId,
            statDate = today,
            parentMessage = "이미 캐싱된 부모 메시지",
            childMessage = "이미 캐싱된 자녀 메시지",
            actionType = DailyActivityRecommendationActionType.CALL,
        )
        recRepo.save(cached)

        val out = service.handle(GetDailyActivityRecommendationQuery.In(userId = userId, statDate = today))

        assertEquals("이미 캐싱된 부모 메시지", out.recommendation.parentMessage)
        assertEquals("이미 캐싱된 자녀 메시지", out.recommendation.childMessage)
        assertEquals(DailyActivityRecommendationActionType.CALL, out.recommendation.actionType)
        assertEquals(0, aiGenerator.callCount)
    }

    @Test
    fun `returns default recommendation without calling AI when stat is null`() {
        val statRepo = InMemoryDailyActivityStatRepository()
        val recRepo = InMemoryDailyActivityRecommendationRepository()
        val aiGenerator = MockActivityRecommendationGenerator()
        val service = DailyActivityRecommendationService(statRepo, recRepo, aiGenerator)

        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val out = service.handle(GetDailyActivityRecommendationQuery.In(userId = userId, statDate = today))

        assertEquals(DailyActivityRecommendationService.DEFAULT_PARENT_MESSAGE, out.recommendation.parentMessage)
        assertEquals(DailyActivityRecommendationService.DEFAULT_CHILD_MESSAGE, out.recommendation.childMessage)
        assertEquals(DailyActivityRecommendationActionType.NONE, out.recommendation.actionType)
        assertEquals(0, aiGenerator.callCount)
        assertEquals(0, recRepo.store.size) // Not persisted so data arriving later can still trigger AI
    }

    @Test
    fun `generates, saves and returns recommendation when stat exists and not cached`() {
        val statRepo = InMemoryDailyActivityStatRepository()
        val recRepo = InMemoryDailyActivityRecommendationRepository()
        val aiGenerator = MockActivityRecommendationGenerator()
        val service = DailyActivityRecommendationService(statRepo, recRepo, aiGenerator)

        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        val stat = DailyActivityStat(
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
        )
        statRepo.save(stat)

        val out1 = service.handle(GetDailyActivityRecommendationQuery.In(userId = userId, statDate = today))

        assertEquals(1, aiGenerator.callCount)
        assertEquals(100, aiGenerator.lastSleepPercent)
        assertEquals(50, aiGenerator.lastStepsPercent)
        assertEquals(0, aiGenerator.lastOutingPercent)
        assertEquals(100, aiGenerator.lastSocialPercent)

        assertEquals("오늘 오후 산책 어떠세요?", out1.recommendation.parentMessage)
        assertEquals("어머니가 오늘 아직 외출을 못 하셨어요.", out1.recommendation.childMessage)
        assertEquals(DailyActivityRecommendationActionType.WALK, out1.recommendation.actionType)
        assertEquals(1, recRepo.store.size)

        // Second call on same day should hit cache and NOT call AI again
        val out2 = service.handle(GetDailyActivityRecommendationQuery.In(userId = userId, statDate = today))
        assertEquals(1, aiGenerator.callCount)
        assertEquals(out1.recommendation.id, out2.recommendation.id)
    }

    @Test
    fun `returns the concurrently-saved recommendation when the unique index rejects our own save`() {
        val statRepo = InMemoryDailyActivityStatRepository()
        val aiGenerator = MockActivityRecommendationGenerator()
        val userId = UUID.randomUUID()
        val today = LocalDate.now()

        statRepo.save(
            DailyActivityStat(
                userId = userId,
                statDate = today,
                sleepMinutes = 480,
                steps = 8000,
                outingCount = 1,
                socialContactCount = 1,
                sleepGoalMinutes = 480,
                stepsGoal = 8000,
                outingGoal = 1,
                socialGoal = 1,
            ),
        )

        val winner = DailyActivityRecommendation(
            userId = userId,
            statDate = today,
            parentMessage = "동시 요청이 먼저 저장한 메시지",
            childMessage = "동시 요청이 먼저 저장한 메시지",
            actionType = DailyActivityRecommendationActionType.QUIZ,
        )
        val recRepo = RaceSimulatingRecommendationRepository(winner)
        val service = DailyActivityRecommendationService(statRepo, recRepo, aiGenerator)

        val out = service.handle(GetDailyActivityRecommendationQuery.In(userId = userId, statDate = today))

        assertEquals(winner.id, out.recommendation.id)
        assertEquals("동시 요청이 먼저 저장한 메시지", out.recommendation.parentMessage)
    }
}
