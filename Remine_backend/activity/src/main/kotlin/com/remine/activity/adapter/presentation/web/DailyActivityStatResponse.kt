package com.remine.activity.adapter.presentation.web

import com.remine.activity.domain.DailyActivityStat
import java.time.LocalDate
import java.util.UUID

data class DailyActivityStatResponse(
    val id: UUID,
    val userId: UUID,
    val statDate: LocalDate,
    val sleepMinutes: Int,
    val steps: Int,
    val outingCount: Int,
    val socialContactCount: Int,
    val sleepGoalMinutes: Int,
    val stepsGoal: Int,
    val outingGoal: Int,
    val socialGoal: Int,
) {
    companion object {
        fun from(stat: DailyActivityStat): DailyActivityStatResponse =
            DailyActivityStatResponse(
                id = stat.id,
                userId = stat.userId,
                statDate = stat.statDate,
                sleepMinutes = stat.sleepMinutes,
                steps = stat.steps,
                outingCount = stat.outingCount,
                socialContactCount = stat.socialContactCount,
                sleepGoalMinutes = stat.sleepGoalMinutes,
                stepsGoal = stat.stepsGoal,
                outingGoal = stat.outingGoal,
                socialGoal = stat.socialGoal,
            )
    }
}
