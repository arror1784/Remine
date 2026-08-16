package com.remine.notification.application.port.inbound

import com.remine.notification.domain.Notification
import java.util.UUID

interface CreateNotificationCommand {
    fun handle(command: In): Out

    data class In(
        val recipientUserId: UUID,
        val emoji: String,
        val bgColor: String,
        val title: String,
        val description: String,
        val deepLink: String,
    )

    data class Out(
        val entity: Notification,
    )
}
