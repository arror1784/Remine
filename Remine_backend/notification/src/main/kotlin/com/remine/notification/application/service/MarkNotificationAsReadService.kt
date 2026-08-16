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

        val updated = notification.markAsRead()
        val saved = notificationRepositoryPort.save(updated)
        return MarkNotificationAsReadCommand.Out(entity = saved)
    }
}
