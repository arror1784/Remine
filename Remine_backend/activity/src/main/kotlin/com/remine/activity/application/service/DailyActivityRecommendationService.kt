package com.remine.activity.application.service

import com.remine.activity.application.port.inbound.GetDailyActivityRecommendationQuery
import com.remine.activity.application.port.outbound.ActivityRecommendationGeneratorPort
import com.remine.activity.application.port.outbound.DailyActivityRecommendationRepositoryPort
import com.remine.activity.application.port.outbound.DailyActivityStatRepositoryPort
import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class DailyActivityRecommendationService(
    private val dailyActivityStatRepository: DailyActivityStatRepositoryPort,
    private val dailyActivityRecommendationRepository: DailyActivityRecommendationRepositoryPort,
    private val activityRecommendationGenerator: ActivityRecommendationGeneratorPort,
) : GetDailyActivityRecommendationQuery {

    override fun handle(query: GetDailyActivityRecommendationQuery.In): GetDailyActivityRecommendationQuery.Out {
        val targetDate = query.statDate ?: LocalDate.now()
        val cached = dailyActivityRecommendationRepository.findByUserIdAndStatDate(query.userId, targetDate)
        if (cached != null) {
            return GetDailyActivityRecommendationQuery.Out(recommendation = cached)
        }

        val stat = dailyActivityStatRepository.findByUserIdAndStatDate(query.userId, targetDate)
        if (stat == null) {
            return GetDailyActivityRecommendationQuery.Out(
                recommendation = DailyActivityRecommendation(
                    userId = query.userId,
                    statDate = targetDate,
                    parentMessage = DEFAULT_PARENT_MESSAGE,
                    childMessage = DEFAULT_CHILD_MESSAGE,
                    actionType = DailyActivityRecommendationActionType.NONE,
                )
            )
        }

        fun calcPercent(value: Int, goal: Int): Int =
            if (goal > 0) minOf(100, (value * 100) / goal) else 0

        val sleepPercent = calcPercent(stat.sleepMinutes, stat.sleepGoalMinutes)
        val stepsPercent = calcPercent(stat.steps, stat.stepsGoal)
        val outingPercent = calcPercent(stat.outingCount, stat.outingGoal)
        val socialPercent = calcPercent(stat.socialContactCount, stat.socialGoal)

        val generated = activityRecommendationGenerator.generateRecommendation(
            stat = stat,
            sleepPercent = sleepPercent,
            stepsPercent = stepsPercent,
            outingPercent = outingPercent,
            socialPercent = socialPercent,
        )

        val recommendation = DailyActivityRecommendation(
            userId = query.userId,
            statDate = targetDate,
            parentMessage = generated.parentMessage,
            childMessage = generated.childMessage,
            actionType = generated.actionType,
        )

        val saved = try {
            dailyActivityRecommendationRepository.save(recommendation)
        } catch (e: DataIntegrityViolationException) {
            // Another concurrent request for the same user+date won the (userId, statDate)
            // unique index between our cache-miss check above and this save — use whichever
            // row it wrote instead of failing this request or double-billing the AI call.
            dailyActivityRecommendationRepository.findByUserIdAndStatDate(query.userId, targetDate) ?: throw e
        }
        return GetDailyActivityRecommendationQuery.Out(recommendation = saved)
    }

    companion object {
        const val DEFAULT_PARENT_MESSAGE = "오늘 하루도 활기차게 시작해보세요!"
        const val DEFAULT_CHILD_MESSAGE = "오늘 부모님의 활동 기록이 아직 없습니다."
    }
}
