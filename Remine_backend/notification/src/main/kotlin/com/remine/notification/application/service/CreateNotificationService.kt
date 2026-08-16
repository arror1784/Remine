package com.remine.notification.application.service

import com.remine.notification.application.port.inbound.CreateNotificationCommand
import com.remine.notification.application.port.outbound.NotificationRepositoryPort
import com.remine.notification.domain.Notification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateNotificationService(
    private val notificationRepositoryPort: NotificationRepositoryPort,
) : CreateNotificationCommand {

    override fun handle(command: CreateNotificationCommand.In): CreateNotificationCommand.Out {
        val notification = Notification(
            recipientUserId = command.recipientUserId,
            emoji = command.emoji,
            bgColor = command.bgColor,
            title = command.title,
            description = command.description,
            deepLink = command.deepLink,
        )
        val saved = notificationRepositoryPort.save(notification)
        return CreateNotificationCommand.Out(entity = saved)
    }
}
