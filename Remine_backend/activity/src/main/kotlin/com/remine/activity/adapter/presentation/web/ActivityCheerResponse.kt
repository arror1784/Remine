package com.remine.activity.adapter.presentation.web

import com.remine.activity.domain.ActivityCheer
import java.time.Instant
import java.util.UUID

data class ActivityCheerResponse(
    val id: UUID,
    val checklistItemId: UUID,
    val senderUserId: UUID,
    val sentAt: Instant,
) {
    companion object {
        fun from(cheer: ActivityCheer): ActivityCheerResponse =
            ActivityCheerResponse(
                id = cheer.id,
                checklistItemId = cheer.checklistItemId,
                senderUserId = cheer.senderUserId,
                sentAt = cheer.sentAt,
            )
    }
}
