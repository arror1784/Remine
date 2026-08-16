package com.remine.message.domain

import java.time.Instant
import java.util.UUID

data class ChatMessage(
    val id: UUID = UUID.randomUUID(),
    val senderId: UUID,
    val recipientId: UUID,
    val body: String,
    val quickReplyKey: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
