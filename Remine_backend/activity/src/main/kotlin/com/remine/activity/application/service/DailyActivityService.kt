package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetTodaySummaryQuery
import com.remine.activity.application.port.inbound.GetWeeklyPatternQuery
import com.remine.activity.application.port.inbound.RecordDailyActivityCommand
import com.remine.activity.application.port.inbound.SyncDailyActivityCommand
import com.remine.activity.application.port.inbound.UpdateDailyActivityCommand
import com.remine.activity.application.port.outbound.DailyActivityRecommendationRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.DailyActivityStat
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
@Transactional
class DailyActivityService(
    private val dailyActivityStatRepository: DailyActivityStatRepositoryPort,
    private val dailyActivityRecommendationRepository: DailyActivityRecommendationRepositoryPort,
) : RecordDailyActivityCommand,
    UpdateDailyActivityCommand,
    SyncDailyActivityCommand,
    GetTodaySummaryQuery,
    GetWeeklyPatternQuery {

    override fun handle(command: RecordDailyActivityCommand.In): RecordDailyActivityCommand.Out {
        val existing = dailyActivityStatRepository.findByUserIdAndStatDate(command.userId, command.statDate)
        if (existing != null) {
            throw InvalidRequestException("Daily activity stat already exists for user ${command.userId} on ${command.statDate}")
        }
        val entity = DailyActivityStat(
            userId = command.userId,
            statDate = command.statDate,
            sleepMinutes = command.sleepMinutes,
            steps = command.steps,
            outingCount = command.outingCount,
            socialContactCount = command.socialContactCount,
        )
        val saved = dailyActivityStatRepository.save(entity)
        // Stale-cache guard: a recommendation may already be cached for this date (e.g. the
        // stat-less default path doesn't persist one, but a retry after a partial sync could
        // have). Drop it so the next read regenerates against the stat just recorded.
        dailyActivityRecommendationRepository.deleteByUserIdAndStatDate(command.userId, command.statDate)
        return RecordDailyActivityCommand.Out(entity = saved)
    }

    override fun handle(command: UpdateDailyActivityCommand.In): UpdateDailyActivityCommand.Out {
        val existing = dailyActivityStatRepository.findByUserIdAndStatDate(command.userId, command.statDate)
            ?: throw EntityNotFoundException("Daily activity stat not found for user ${command.userId} on ${command.statDate}")

        val updated = existing.copy(
            sleepMinutes = command.sleepMinutes ?: existing.sleepMinutes,
            steps = command.steps ?: existing.steps,
            outingCount = command.outingCount ?: existing.outingCount,
            socialContactCount = command.socialContactCount ?: existing.socialContactCount,
            updatedAt = Instant.now(),
        )
        val saved = dailyActivityStatRepository.save(updated)
        // The cached recommendation (if any) was generated from the stat values this just
        // changed — drop it so the next read reflects the update instead of serving stale advice.
        dailyActivityRecommendationRepository.deleteByUserIdAndStatDate(command.userId, command.statDate)
        return UpdateDailyActivityCommand.Out(entity = saved)
    }

    override fun handle(command: SyncDailyActivityCommand.In): SyncDailyActivityCommand.Out {
        if (command.entries.isEmpty()) {
            return SyncDailyActivityCommand.Out(savedCount = 0)
        }
        val statDates = command.entries.map { it.statDate }.toSet()
        val existingMap = dailyActivityStatRepository.findByUserIdAndStatDateIn(command.userId, statDates)
            .associateBy { it.statDate }

        val entitiesToSave = command.entries.map { entry ->
            val existing = existingMap[entry.statDate]
            if (existing != null) {
                existing.copy(
                    sleepMinutes = entry.sleepMinutes,
                    steps = entry.steps,
                    outingCount = entry.outingCount,
                    socialContactCount = entry.socialContactCount,
                    updatedAt = Instant.now(),
                )
            } else {
                DailyActivityStat(
                    userId = command.userId,
                    statDate = entry.statDate,
                    sleepMinutes = entry.sleepMinutes,
                    steps = entry.steps,
                    outingCount = entry.outingCount,
                    socialContactCount = entry.socialContactCount,
                )
            }
        }
        val saved = dailyActivityStatRepository.saveAll(entitiesToSave)
        // Same staleness guard as the single-day paths, for every date this batch touched.
        statDates.forEach { dailyActivityRecommendationRepository.deleteByUserIdAndStatDate(command.userId, it) }
        return SyncDailyActivityCommand.Out(savedCount = saved.size)
    }

    @Transactional(readOnly = true)
    override fun handle(query: GetTodaySummaryQuery.In): GetTodaySummaryQuery.Out {
        val today = LocalDate.now()
        val stat = dailyActivityStatRepository.findByUserIdAndStatDate(query.userId, today)
        if (stat == null) {
            return GetTodaySummaryQuery.Out(
                stat = null,
                sleepPercent = 0,
                stepsPercent = 0,
                outingPercent = 0,
                socialPercent = 0,
            )
        }

        fun calcPercent(value: Int, goal: Int): Int =
            if (goal > 0) minOf(100, (value * 100) / goal) else 0

        return GetTodaySummaryQuery.Out(
            stat = stat,
            sleepPercent = calcPercent(stat.sleepMinutes, stat.sleepGoalMinutes),
            stepsPercent = calcPercent(stat.steps, stat.stepsGoal),
            outingPercent = calcPercent(stat.outingCount, stat.outingGoal),
            socialPercent = calcPercent(stat.socialContactCount, stat.socialGoal),
        )
    }

    @Transactional(readOnly = true)
    override fun handle(query: GetWeeklyPatternQuery.In): GetWeeklyPatternQuery.Out {
        val today = LocalDate.now()
        val startDate = today.minusDays(6)
        val existingMap = dailyActivityStatRepository.findByUserIdAndStatDateBetween(query.userId, startDate, today)
            .associateBy { it.statDate }

        val days = (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val steps = existingMap[date]?.steps ?: 0
            GetWeeklyPatternQuery.DayPoint(
                statDate = date,
                steps = steps,
                isToday = date.isEqual(today),
            )
        }
        return GetWeeklyPatternQuery.Out(days = days)
    }
}
