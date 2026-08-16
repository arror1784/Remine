package com.remine.activity.application.port.outbound

import com.remine.activity.domain.ActivityCheer
import java.time.Instant
import java.util.UUID

interface ActivityCheerRepositoryPort {
    fun findByChecklistItemIdAndSenderUserId(
        checklistItemId: UUID,
        senderUserId: UUID,
    ): List<ActivityCheer>

    fun findByChecklistItemIdAndSenderUserIdAndSentAtBetween(
        checklistItemId: UUID,
        senderUserId: UUID,
        startOfDay: Instant,
        endOfDay: Instant,
    ): List<ActivityCheer>

    fun save(cheer: ActivityCheer): ActivityCheer
}
