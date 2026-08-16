package com.remine.activity.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface ActivityCheerJpaRepository : JpaRepository<ActivityCheerJpaEntity, UUID> {
    fun findByChecklistItemIdAndSenderUserId(
        checklistItemId: UUID,
        senderUserId: UUID,
    ): List<ActivityCheerJpaEntity>

    fun findByChecklistItemIdAndSenderUserIdAndSentAtBetween(
        checklistItemId: UUID,
        senderUserId: UUID,
        startOfDay: Instant,
        endOfDay: Instant,
    ): List<ActivityCheerJpaEntity>
}
