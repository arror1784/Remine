package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.GetNotificationsQuery
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetNotificationsService(
    private val notificationRepositoryPort: NotificationRepositoryPort,
) : GetNotificationsQuery {

    override fun handle(query: GetNotificationsQuery.In): GetNotificationsQuery.Out {
        val items = notificationRepositoryPort.findAllByRecipientUserIdOrderByCreatedAtDesc(query.recipientUserId)
        return GetNotificationsQuery.Out(items = items)
    }
}
