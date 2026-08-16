package com.remine.activity.application.port.inbound

import com.remine.activity.domain.DailyActivityRecommendation
import java.time.LocalDate
import java.util.UUID

interface GetDailyActivityRecommendationQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
        val statDate: LocalDate? = null,
    )

    data class Out(
        val recommendation: DailyActivityRecommendation,
    )
}
