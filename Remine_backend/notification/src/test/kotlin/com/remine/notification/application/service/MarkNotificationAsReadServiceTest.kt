package com.remine.notification.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.notification.application.port.inbound.MarkNotificationAsReadCommand
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class MarkNotificationAsReadServiceTest {

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
    fun `marks notification as read successfully`() {
        val repo = FakeNotificationRepository()
        val service = MarkNotificationAsReadService(repo)

        val userId = UUID.randomUUID()
        val notification = Notification(
            recipientUserId = userId,
            emoji = "🌸",
            bgColor = "#fff7cc",
            title = "딸 지영님이 사진을 추가했어요",
            description = "2019년 봄 나들이 사진이 업로드됐어요.",
            deepLink = "/parent/memories",
            read = false,
        )
        repo.save(notification)

        val result = service.handle(
            MarkNotificationAsReadCommand.In(
                notificationId = notification.id,
                recipientUserId = userId,
            )
        )

        assertTrue(result.entity.read)
        assertEquals(notification.id, result.entity.id)
        assertEquals(notification.title, result.entity.title)
    }

    @Test
    fun `throws EntityNotFoundException when notification does not exist`() {
        val repo = FakeNotificationRepository()
        val service = MarkNotificationAsReadService(repo)

        assertThrows(EntityNotFoundException::class.java) {
            service.handle(
                MarkNotificationAsReadCommand.In(
                    notificationId = UUID.randomUUID(),
                    recipientUserId = UUID.randomUUID(),
                )
            )
        }
    }

    @Test
    fun `throws EntityNotFoundException when recipientUserId does not match`() {
        val repo = FakeNotificationRepository()
        val service = MarkNotificationAsReadService(repo)

        val ownerId = UUID.randomUUID()
        val otherUserId = UUID.randomUUID()
        val notification = Notification(
            recipientUserId = ownerId,
            emoji = "🌸",
            bgColor = "#fff7cc",
            title = "딸 지영님이 사진을 추가했어요",
            description = "2019년 봄 나들이 사진이 업로드됐어요.",
            deepLink = "/parent/memories",
            read = false,
        )
        repo.save(notification)

        assertThrows(EntityNotFoundException::class.java) {
            service.handle(
                MarkNotificationAsReadCommand.In(
                    notificationId = notification.id,
                    recipientUserId = otherUserId,
                )
            )
        }
    }
}
