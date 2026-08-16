package com.remine.notification.application.port.inbound

import java.util.UUID

interface GetUnreadNotificationCountQuery {
    fun handle(query: In): Out

    data class In(
        val recipientUserId: UUID,
    )

    data class Out(
        val count: Int,
    )
}
