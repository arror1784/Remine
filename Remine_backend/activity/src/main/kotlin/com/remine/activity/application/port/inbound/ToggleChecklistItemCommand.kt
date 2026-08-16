package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityChecklistItem
import java.util.UUID

interface ToggleChecklistItemCommand {
    fun handle(command: In): Out

    data class In(
        val checklistItemId: UUID,
        val done: Boolean,
        /** The parent whose checklist the caller is entitled to touch (the caller if PARENT, their paired parent if CHILD). */
        val requestedByParentUserId: UUID,
    )

    data class Out(
        val entity: ActivityChecklistItem,
    )
}
