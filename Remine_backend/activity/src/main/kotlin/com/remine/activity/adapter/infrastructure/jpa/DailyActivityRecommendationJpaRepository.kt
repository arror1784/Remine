package com.remine.activity.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.UUID

interface DailyActivityRecommendationJpaRepository : JpaRepository<DailyActivityRecommendationJpaEntity, UUID> {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendationJpaEntity?
    fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate)
}
