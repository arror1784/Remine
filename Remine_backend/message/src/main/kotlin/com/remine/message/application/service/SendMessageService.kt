package com.remine.message.application.service

import com.remine.message.application.port.inbound.SendMessageCommand
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.domain.ChatMessage
import com.remine.notification.application.port.inbound.CreateNotificationCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SendMessageService(
    private val chatMessageRepositoryPort: ChatMessageRepositoryPort,
    private val createNotificationCommand: CreateNotificationCommand,
) : SendMessageCommand {

    override fun handle(command: SendMessageCommand.In): SendMessageCommand.Out {
        val chatMessage = ChatMessage(
            senderId = command.senderId,
            recipientId = command.recipientId,
            body = command.body,
            quickReplyKey = command.quickReplyKey,
        )
        val saved = chatMessageRepositoryPort.save(chatMessage)

        createNotificationCommand.handle(
            CreateNotificationCommand.In(
                recipientUserId = command.recipientId,
                emoji = "💬",
                bgColor = "#fff7cc",
                title = "새 메시지가 도착했어요",
                description = command.body.take(80),
                deepLink = "family/message",
            ),
        )

        return SendMessageCommand.Out(entity = saved)
    }
}
