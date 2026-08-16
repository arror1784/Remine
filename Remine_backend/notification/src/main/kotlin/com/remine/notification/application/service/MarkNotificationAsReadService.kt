package com.remine.notification.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.notification.application.port.inbound.MarkNotificationAsReadCommand
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MarkNotificationAsReadService(
    private val notificationRepositoryPort: NotificationRepositoryPort,
) : MarkNotificationAsReadCommand {

    override fun handle(command: MarkNotificationAsReadCommand.In): MarkNotificationAsReadCommand.Out {
        val notification = notificationRepositoryPort.findByIdAndRecipientUserId(
            id = command.notificationId,
            recipientUserId = command.recipientUserId,
        ) ?: throw EntityNotFoundException("Notification not found with id: ${command.notificationId} for user: ${command.recipientUserId}")

        val saved = notificationRepositoryPort.save(notification.markAsRead())

        // Reading one notification also clears every other unread notification of the same
        // "kind" (there's no separate type field, so deepLink — the destination it points to —
        // stands in for it) for this recipient, since they've now seen that context.
        notificationRepositoryPort.findAllByRecipientUserIdOrderByCreatedAtDesc(command.recipientUserId)
            .filter { !it.read && it.id != saved.id && it.deepLink == saved.deepLink }
            .forEach { notificationRepositoryPort.save(it.markAsRead()) }

        return MarkNotificationAsReadCommand.Out(entity = saved)
    }
}
