package com.remine.notification.adapter.presentation.web

import com.remine.notification.domain.Notification
import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val recipientUserId: UUID,
    val emoji: String,
    val bgColor: String,
    val title: String,
    val description: String,
    val deepLink: String,
    val read: Boolean,
    val createdAt: Instant,
) {
    companion object {
        fun from(domain: Notification): NotificationResponse = NotificationResponse(
            id = domain.id,
            recipientUserId = domain.recipientUserId,
            emoji = domain.emoji,
            bgColor = domain.bgColor,
            title = domain.title,
            description = domain.description,
            deepLink = domain.deepLink,
            read = domain.read,
            createdAt = domain.createdAt,
        )
    }
}
