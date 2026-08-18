package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.application.port.outbound.ActivityTimelineEventRepositoryPort
import com.remine.activity.domain.ActivityTimelineEvent
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class ActivityTimelineEventRepositoryAdapter(
    private val jpaRepository: ActivityTimelineEventJpaRepository,
) : ActivityTimelineEventRepositoryPort {

    override fun findByUserIdAndStatDateOrderByOccurredAtAsc(
        userId: UUID,
        statDate: LocalDate,
    ): List<ActivityTimelineEvent> {
        return jpaRepository.findByUserIdAndStatDateOrderByOccurredAtAsc(userId, statDate).map { it.toDomain() }
    }

    override fun save(event: ActivityTimelineEvent): ActivityTimelineEvent {
        val entity = ActivityTimelineEventJpaEntity.fromDomain(event)
        return jpaRepository.save(entity).toDomain()
    }

    override fun deleteAllByUserId(userId: UUID) {
        val entities = jpaRepository.findAllByUserId(userId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
