package com.remine.message.application.port.outbound

import com.remine.message.domain.ChatMessage
import java.time.Instant
import java.util.UUID

interface ChatMessageRepositoryPort {
    fun save(chatMessage: ChatMessage): ChatMessage
    fun findThread(userAId: UUID, userBId: UUID, before: Instant?, limit: Int): List<ChatMessage>
    fun countByPair(userAId: UUID, userBId: UUID): Int
}
