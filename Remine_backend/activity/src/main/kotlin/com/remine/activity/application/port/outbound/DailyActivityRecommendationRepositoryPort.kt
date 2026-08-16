package com.remine.activity.application.port.outbound

import com.remine.activity.domain.DailyActivityRecommendation
import java.time.LocalDate
import java.util.UUID

interface DailyActivityRecommendationRepositoryPort {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendation?
    fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation
}
