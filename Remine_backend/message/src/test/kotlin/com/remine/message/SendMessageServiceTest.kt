package com.remine.message

import com.remine.message.application.port.inbound.SendMessageCommand
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.application.service.SendMessageService
import com.remine.message.domain.ChatMessage
import com.remine.notification.application.port.inbound.CreateNotificationCommand
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    private val fakeCreateNotification = object : CreateNotificationCommand {
        val created = mutableListOf<CreateNotificationCommand.In>()

        override fun handle(command: CreateNotificationCommand.In): CreateNotificationCommand.Out {
            created.add(command)
            return CreateNotificationCommand.Out(
                entity = Notification(
                    recipientUserId = command.recipientUserId,
                    emoji = command.emoji,
                    bgColor = command.bgColor,
                    title = command.title,
                    description = command.description,
                    deepLink = command.deepLink,
                )
            )
        }
    }

    private val service = SendMessageService(fakeRepo, fakeCreateNotification)

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

    @Test
    fun `should create a notification for the recipient`() {
        val senderId = UUID.randomUUID()
        val recipientId = UUID.randomUUID()

        service.handle(
            SendMessageCommand.In(
                senderId = senderId,
                recipientId = recipientId,
                body = "밥 먹었니?",
            )
        )

        assertEquals(1, fakeCreateNotification.created.size)
        val notification = fakeCreateNotification.created.first()
        assertEquals(recipientId, notification.recipientUserId)
        assertEquals("💬", notification.emoji)
        assertEquals("새 메시지가 도착했어요", notification.title)
        assertEquals("밥 먹었니?", notification.description)
        assertEquals("family/message", notification.deepLink)
        assertFalse(notification.deepLink.startsWith("/"))
    }

    @Test
    fun `should truncate long message bodies in the notification description`() {
        val body = "가".repeat(200)

        service.handle(
            SendMessageCommand.In(
                senderId = UUID.randomUUID(),
                recipientId = UUID.randomUUID(),
                body = body,
            )
        )

        assertEquals(80, fakeCreateNotification.created.last().description.length)
    }
}
