package com.remine.message

import com.remine.message.application.port.inbound.CountChatThreadQuery
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.application.service.CountChatThreadService
import com.remine.message.domain.ChatMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CountChatThreadServiceTest {

    private val userA = UUID.randomUUID()
    private val userB = UUID.randomUUID()

    private val fakeRepo = object : ChatMessageRepositoryPort {
        override fun save(chatMessage: ChatMessage): ChatMessage = chatMessage

        override fun findThread(userAId: UUID, userBId: UUID, before: Instant?, limit: Int): List<ChatMessage> = emptyList()

        override fun countByPair(userAId: UUID, userBId: UUID): Int = 1234
    }

    private val service = CountChatThreadService(fakeRepo)

    @Test
    fun `should return the pair's message count from the repository`() {
        val result = service.handle(CountChatThreadQuery.In(userAId = userA, userBId = userB))

        assertEquals(1234, result.count)
    }
}
