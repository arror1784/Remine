package com.remine.notification.application.port.outbound

import com.remine.notification.domain.Notification
import java.util.UUID

interface NotificationRepositoryPort {
    fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): Notification?
    fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification>
    fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int
    fun save(notification: Notification): Notification
}
