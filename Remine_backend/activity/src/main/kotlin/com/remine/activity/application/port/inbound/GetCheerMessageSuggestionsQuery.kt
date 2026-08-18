package com.remine.activity.application.port.inbound

import java.util.UUID

interface GetCheerMessageSuggestionsQuery {
    fun handle(query: In): Out

    data class In(
        val checklistItemId: UUID,
        /** The parent whose checklist the caller is entitled to touch (the caller if PARENT, their paired parent if CHILD). */
        val requestedByParentUserId: UUID,
    )

    data class Out(
        val suggestions: List<String>,
    )
}
