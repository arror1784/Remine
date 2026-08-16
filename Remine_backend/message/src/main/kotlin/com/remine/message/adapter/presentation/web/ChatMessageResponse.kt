package com.remine.message.adapter.presentation.web

import com.remine.message.domain.ChatMessage
import java.time.Instant
import java.util.UUID

data class ChatMessageResponse(
    val id: UUID,
    val senderId: UUID,
    val recipientId: UUID,
    val body: String,
    val quickReplyKey: String?,
    val createdAt: Instant,
) {
    companion object {
        fun from(domain: ChatMessage): ChatMessageResponse = ChatMessageResponse(
            id = domain.id,
            senderId = domain.senderId,
            recipientId = domain.recipientId,
            body = domain.body,
            quickReplyKey = domain.quickReplyKey,
            createdAt = domain.createdAt,
        )
    }
}
