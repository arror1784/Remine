package com.remine.notification.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<NotificationJpaEntity, UUID> {
    fun findByIdAndRecipientUserId(id: UUID, recipientUserId: UUID): NotificationJpaEntity?
    fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<NotificationJpaEntity>
    fun countByRecipientUserIdAndReadFalse(recipientUserId: UUID): Int
}
