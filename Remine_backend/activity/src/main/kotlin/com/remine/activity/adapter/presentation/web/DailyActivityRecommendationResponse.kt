package com.remine.activity.adapter.presentation.web

import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import java.time.LocalDate
import java.util.UUID

data class DailyActivityRecommendationResponse(
    val id: UUID?,
    val userId: UUID,
    val statDate: LocalDate,
    val parentMessage: String,
    val childMessage: String,
    val actionType: DailyActivityRecommendationActionType,
) {
    companion object {
        fun from(domain: DailyActivityRecommendation): DailyActivityRecommendationResponse =
            DailyActivityRecommendationResponse(
                id = domain.id,
                userId = domain.userId,
                statDate = domain.statDate,
                parentMessage = domain.parentMessage,
                childMessage = domain.childMessage,
                actionType = domain.actionType,
            )
    }
}
