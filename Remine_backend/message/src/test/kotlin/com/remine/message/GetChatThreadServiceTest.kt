package com.remine.message

import com.remine.message.application.port.inbound.GetChatThreadQuery
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.application.service.GetChatThreadService
import com.remine.message.domain.ChatMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class GetChatThreadServiceTest {

    private val userA = UUID.randomUUID()
    private val userB = UUID.randomUUID()

    private val fakeRepo = object : ChatMessageRepositoryPort {
        override fun save(chatMessage: ChatMessage): ChatMessage = chatMessage

        override fun findThread(userAId: UUID, userBId: UUID, before: Instant?, limit: Int): List<ChatMessage> {
            return listOf(
                ChatMessage(senderId = userAId, recipientId = userBId, body = "Message 1"),
                ChatMessage(senderId = userBId, recipientId = userAId, body = "Message 2"),
            )
        }

        override fun countByPair(userAId: UUID, userBId: UUID): Int = 2

        override fun deleteAllByParticipant(userId: UUID) {}
    }

    private val service = GetChatThreadService(fakeRepo)

    @Test
    fun `should return thread messages`() {
        val result = service.handle(
            GetChatThreadQuery.In(
                userAId = userA,
                userBId = userB,
                before = null,
                limit = 50,
            )
        )

        assertEquals(2, result.items.size)
        assertEquals("Message 1", result.items[0].body)
        assertEquals("Message 2", result.items[1].body)
    }
}
