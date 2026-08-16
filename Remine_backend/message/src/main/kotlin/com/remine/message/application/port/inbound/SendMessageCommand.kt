package com.remine.message.application.port.inbound

import com.remine.message.domain.ChatMessage
import java.util.UUID

interface SendMessageCommand {
    fun handle(command: In): Out

    data class In(
        val senderId: UUID,
        val recipientId: UUID,
        val body: String,
        val quickReplyKey: String? = null,
    )

    data class Out(
        val entity: ChatMessage,
    )
}
