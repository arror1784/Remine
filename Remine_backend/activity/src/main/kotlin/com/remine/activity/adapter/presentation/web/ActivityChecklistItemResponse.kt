package com.remine.activity.adapter.presentation.web

import com.remine.activity.domain.ActivityChecklistItem
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class ActivityChecklistItemResponse(
    val id: UUID,
    val userId: UUID,
    val statDate: LocalDate,
    val type: String,
    val done: Boolean,
    val completedAt: Instant?,
    val note: String?,
) {
    companion object {
        fun from(item: ActivityChecklistItem): ActivityChecklistItemResponse =
            ActivityChecklistItemResponse(
                id = item.id,
                userId = item.userId,
                statDate = item.statDate,
                type = item.type,
                done = item.done,
                completedAt = item.completedAt,
                note = item.note,
            )
    }
}
