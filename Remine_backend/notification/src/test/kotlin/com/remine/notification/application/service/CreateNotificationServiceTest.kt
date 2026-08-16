package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.CreateNotificationCommand
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateNotificationServiceTest {

    private class FakeNotificationRepository : NotificationRepositoryPort {
        val storage = mutableMapOf<UUID, Notification>()

        override fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): Notification? {
            val notification = storage[id]
            return if (notification != null && notification.recipientUserId == recipientUserId) notification else null
        }

        override fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification> {
            return storage.values.filter { it.recipientUserId == recipientUserId }
        }

        override fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int {
            return storage.values.count { it.recipientUserId == recipientUserId && !it.read }
        }

        override fun save(notification: Notification): Notification {
            storage[notification.id] = notification
            return notification
        }
    }

    @Test
    fun `creates and saves an unread notification for the recipient`() {
        val repo = FakeNotificationRepository()
        val service = CreateNotificationService(repo)

        val recipientUserId = UUID.randomUUID()

        val result = service.handle(
            CreateNotificationCommand.In(
                recipientUserId = recipientUserId,
                emoji = "💬",
                bgColor = "#fff7cc",
                title = "새 메시지가 도착했어요",
                description = "밥 먹었니?",
                deepLink = "family/message",
            )
        )

        assertNotNull(result.entity.id)
        assertEquals(recipientUserId, result.entity.recipientUserId)
        assertEquals("💬", result.entity.emoji)
        assertEquals("#fff7cc", result.entity.bgColor)
        assertEquals("새 메시지가 도착했어요", result.entity.title)
        assertEquals("밥 먹었니?", result.entity.description)
        assertEquals("family/message", result.entity.deepLink)
        assertFalse(result.entity.read)

        assertEquals(1, repo.storage.size)
        assertEquals(result.entity, repo.storage[result.entity.id])
    }
}
