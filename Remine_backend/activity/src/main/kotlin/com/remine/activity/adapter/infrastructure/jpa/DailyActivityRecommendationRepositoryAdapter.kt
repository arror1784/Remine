package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.application.port.outbound.DailyActivityRecommendationRepositoryPort
import com.remine.activity.domain.DailyActivityRecommendation
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class DailyActivityRecommendationRepositoryAdapter(
    private val jpaRepository: DailyActivityRecommendationJpaRepository,
) : DailyActivityRecommendationRepositoryPort {

    override fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityRecommendation? {
        return jpaRepository.findByUserIdAndStatDate(userId, statDate)?.toDomain()
    }

    override fun save(recommendation: DailyActivityRecommendation): DailyActivityRecommendation {
        val entity = DailyActivityRecommendationJpaEntity.fromDomain(recommendation)
        return jpaRepository.save(entity).toDomain()
    }
}
