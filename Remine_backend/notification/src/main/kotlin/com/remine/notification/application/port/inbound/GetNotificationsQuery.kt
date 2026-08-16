package com.remine.notification.application.port.inbound

import com.remine.notification.domain.Notification
import java.util.UUID

interface GetNotificationsQuery {
    fun handle(query: In): Out

    data class In(
        val recipientUserId: UUID,
    )

    data class Out(
        val items: List<Notification>,
    )
}
