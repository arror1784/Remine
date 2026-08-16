package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityCheer
import java.util.UUID

interface SendCheerCommand {
    fun handle(command: In): Out

    data class In(
        val checklistItemId: UUID,
        val senderUserId: UUID,
        /** The parent whose checklist the caller is entitled to touch (the caller if PARENT, their paired parent if CHILD). */
        val requestedByParentUserId: UUID,
    )

    data class Out(
        val entity: ActivityCheer?,
    )
}
