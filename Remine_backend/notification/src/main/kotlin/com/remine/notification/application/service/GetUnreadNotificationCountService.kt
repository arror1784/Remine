package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.GetUnreadNotificationCountQuery
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetUnreadNotificationCountService(
    private val notificationRepositoryPort: NotificationRepositoryPort,
) : GetUnreadNotificationCountQuery {

    override fun handle(query: GetUnreadNotificationCountQuery.In): GetUnreadNotificationCountQuery.Out {
        val count = notificationRepositoryPort.countByRecipientUserIdAndReadFalse(query.recipientUserId)
        return GetUnreadNotificationCountQuery.Out(count = count)
    }
}
