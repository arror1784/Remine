package com.remine.message.adapter.presentation.web

import com.remine.message.domain.QuickReply
import java.util.UUID

data class QuickReplyResponse(
    val id: UUID,
    val role: String,
    val label: String,
    val sortOrder: Int,
) {
    companion object {
        fun from(domain: QuickReply): QuickReplyResponse = QuickReplyResponse(
            id = domain.id,
            role = domain.role,
            label = domain.label,
            sortOrder = domain.sortOrder,
        )
    }
}
