package com.remine.message

import com.remine.message.application.port.inbound.SendMessageCommand
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.application.service.SendMessageService
import com.remine.message.domain.ChatMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class SendMessageServiceTest {

    private val fakeRepo = object : ChatMessageRepositoryPort {
        val messages = mutableListOf<ChatMessage>()

        override fun save(chatMessage: ChatMessage): ChatMessage {
            messages.add(chatMessage)
            return chatMessage
        }

        override fun findThread(userAId: UUID, userBId: UUID, before: java.time.Instant?, limit: Int): List<ChatMessage> {
            return messages
        }
    }

    private val service = SendMessageService(fakeRepo)

    @Test
    fun `should save and return chat message`() {
        val senderId = UUID.randomUUID()
        val recipientId = UUID.randomUUID()

        val result = service.handle(
            SendMessageCommand.In(
                senderId = senderId,
                recipientId = recipientId,
                body = "Hello Mom!",
                quickReplyKey = "quick_1",
            )
        )

        assertNotNull(result.entity.id)
        assertEquals(senderId, result.entity.senderId)
        assertEquals(recipientId, result.entity.recipientId)
        assertEquals("Hello Mom!", result.entity.body)
        assertEquals("quick_1", result.entity.quickReplyKey)
        assertEquals(1, fakeRepo.messages.size)
    }
}
