package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.GetUnreadNotificationCountQuery
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class GetUnreadNotificationCountServiceTest {

    private class FakeNotificationRepository(
        private val count: Int,
    ) : NotificationRepositoryPort {
        override fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): Notification? = null
        override fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification> = emptyList()
        override fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int = count
        override fun save(notification: Notification): Notification = notification
    }

    @Test
    fun `returns unread notification count for user`() {
        val userId = UUID.randomUUID()
        val service = GetUnreadNotificationCountService(FakeNotificationRepository(count = 5))

        val result = service.handle(GetUnreadNotificationCountQuery.In(recipientUserId = userId))

        assertEquals(5, result.count)
    }
}
