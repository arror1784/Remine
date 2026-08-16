package com.remine.message.application.port.inbound

import com.remine.message.domain.ChatMessage
import java.time.Instant
import java.util.UUID

interface GetChatThreadQuery {
    fun handle(query: In): Out

    data class In(
        val userAId: UUID,
        val userBId: UUID,
        val before: Instant? = null,
        val limit: Int = 50,
    )

    data class Out(
        val items: List<ChatMessage>,
    )
}
