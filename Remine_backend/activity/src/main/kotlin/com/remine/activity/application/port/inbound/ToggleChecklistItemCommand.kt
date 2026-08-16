package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityChecklistItem
import java.util.UUID

interface ToggleChecklistItemCommand {
    fun handle(command: In): Out

    data class In(
        val checklistItemId: UUID,
        val done: Boolean,
    )

    data class Out(
        val entity: ActivityChecklistItem,
    )
}
