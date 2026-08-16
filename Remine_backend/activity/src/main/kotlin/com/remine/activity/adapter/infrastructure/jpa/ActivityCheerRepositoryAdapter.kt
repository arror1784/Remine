package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.application.port.outbound.ActivityCheerRepositoryPort
import com.remine.activity.domain.ActivityCheer
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class ActivityCheerRepositoryAdapter(
    private val jpaRepository: ActivityCheerJpaRepository,
) : ActivityCheerRepositoryPort {

    override fun findByChecklistItemIdAndSenderUserId(
        checklistItemId: UUID,
        senderUserId: UUID,
    ): List<ActivityCheer> {
        return jpaRepository.findByChecklistItemIdAndSenderUserId(checklistItemId, senderUserId)
            .map { it.toDomain() }
    }

    override fun findByChecklistItemIdAndSenderUserIdAndSentAtBetween(
        checklistItemId: UUID,
        senderUserId: UUID,
        startOfDay: Instant,
        endOfDay: Instant,
    ): List<ActivityCheer> {
        return jpaRepository.findByChecklistItemIdAndSenderUserIdAndSentAtBetween(
            checklistItemId,
            senderUserId,
            startOfDay,
            endOfDay,
        ).map { it.toDomain() }
    }

    override fun save(cheer: ActivityCheer): ActivityCheer {
        val entity = ActivityCheerJpaEntity.fromDomain(cheer)
        return jpaRepository.save(entity).toDomain()
    }
}
