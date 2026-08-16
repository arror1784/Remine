package com.remine.notification.adapter.infrastructure.jpa

import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationRepositoryAdapter(
    private val notificationJpaRepository: NotificationJpaRepository,
) : NotificationRepositoryPort {

    override fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): Notification? {
        return notificationJpaRepository.findByIdAndRecipientUserId(id, recipientUserId)?.toDomain()
    }

    override fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification> {
        return notificationJpaRepository.findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId)
            .map { it.toDomain() }
    }

    override fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int {
        return notificationJpaRepository.countByRecipientUserIdAndReadFalse(recipientUserId)
    }

    override fun save(notification: Notification): Notification {
        val existing = notificationJpaRepository.findById(notification.id).orElse(null)
        val entity = if (existing != null) {
            existing.apply {
                emoji = notification.emoji
                bgColor = notification.bgColor
                title = notification.title
                description = notification.description
                deepLink = notification.deepLink
                read = notification.read
            }
        } else {
            NotificationJpaEntity.fromDomain(notification)
        }
        return notificationJpaRepository.save(entity).toDomain()
    }
}
