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
        // saveAndFlush, not save: the caller relies on catching the unique-index violation
        // (see DailyActivityRecommendationService) to detect a concurrent generate-and-save
        // race, which only surfaces once the INSERT actually runs — plain save() would defer
        // that to the transaction's end-of-request flush, past where the catch can see it.
        return jpaRepository.saveAndFlush(entity).toDomain()
    }

    override fun deleteByUserIdAndStatDate(userId: UUID, statDate: LocalDate) {
        jpaRepository.deleteByUserIdAndStatDate(userId, statDate)
    }
}
