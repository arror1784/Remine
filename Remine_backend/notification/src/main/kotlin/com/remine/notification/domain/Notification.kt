package com.remine.notification.domain

import java.time.Instant
import java.util.UUID

data class Notification(
    val id: UUID = UUID.randomUUID(),
    val recipientUserId: UUID,
    val emoji: String,
    val bgColor: String,
    val title: String,
    val description: String,
    val deepLink: String,
    val read: Boolean = false,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    fun markAsRead(): Notification = copy(
        read = true,
        updatedAt = Instant.now(),
    )
}
