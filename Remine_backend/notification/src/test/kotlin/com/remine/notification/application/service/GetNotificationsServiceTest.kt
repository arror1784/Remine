package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.GetNotificationsQuery
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class GetNotificationsServiceTest {

    private class FakeNotificationRepository(
        private val items: List<Notification>,
    ) : NotificationRepositoryPort {
        override fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): Notification? =
            items.find { it.id == id && it.recipientUserId == recipientUserId }

        override fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification> =
            items.filter { it.recipientUserId == recipientUserId }.sortedByDescending { it.createdAt }

        override fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int =
            items.count { it.recipientUserId == recipientUserId && !it.read }

        override fun save(notification: Notification): Notification = notification
    }

    @Test
    fun `returns notifications for user ordered by createdAt descending`() {
        val userId = UUID.randomUUID()
        val now = Instant.now()
        val n1 = Notification(
            recipientUserId = userId,
            emoji = "🌸",
            bgColor = "#fff7cc",
            title = "Notification 1",
            description = "Desc 1",
            deepLink = "/to/1",
            createdAt = now.minusSeconds(60),
        )
        val n2 = Notification(
            recipientUserId = userId,
            emoji = "🧩",
            bgColor = "#fff7cc",
            title = "Notification 2",
            description = "Desc 2",
            deepLink = "/to/2",
            createdAt = now,
        )
        val otherUserNotif = Notification(
            recipientUserId = UUID.randomUUID(),
            emoji = "💬",
            bgColor = "#fff7cc",
            title = "Other user",
            description = "Other",
            deepLink = "/to/other",
        )

        val service = GetNotificationsService(FakeNotificationRepository(listOf(n1, n2, otherUserNotif)))
        val result = service.handle(GetNotificationsQuery.In(recipientUserId = userId))

        assertEquals(2, result.items.size)
        assertEquals(n2.id, result.items[0].id)
        assertEquals(n1.id, result.items[1].id)
    }
}
