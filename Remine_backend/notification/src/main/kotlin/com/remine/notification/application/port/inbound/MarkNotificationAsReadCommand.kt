package com.remine.notification.application.port.inbound

import com.remine.notification.domain.Notification
import java.util.UUID

interface MarkNotificationAsReadCommand {
    fun handle(command: In): Out

    data class In(
        val notificationId: UUID,
        val recipientUserId: UUID,
    )

    data class Out(
        val entity: Notification,
    )
}
