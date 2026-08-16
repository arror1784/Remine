package com.remine.activity.application.port.outbound

import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.activity.domain.DailyActivityStat

interface ActivityRecommendationGeneratorPort {
    fun generateRecommendation(
        stat: DailyActivityStat,
        sleepPercent: Int,
        stepsPercent: Int,
        outingPercent: Int,
        socialPercent: Int,
    ): GeneratedRecommendation

    data class GeneratedRecommendation(
        val parentMessage: String,
        val childMessage: String,
        val actionType: DailyActivityRecommendationActionType,
    )
}
