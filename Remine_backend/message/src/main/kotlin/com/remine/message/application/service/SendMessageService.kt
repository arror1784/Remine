package com.remine.message.application.service

import com.remine.message.application.port.inbound.SendMessageCommand
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.domain.ChatMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SendMessageService(
    private val chatMessageRepositoryPort: ChatMessageRepositoryPort,
) : SendMessageCommand {

    override fun handle(command: SendMessageCommand.In): SendMessageCommand.Out {
        val chatMessage = ChatMessage(
            senderId = command.senderId,
            recipientId = command.recipientId,
            body = command.body,
            quickReplyKey = command.quickReplyKey,
        )
        val saved = chatMessageRepositoryPort.save(chatMessage)
        return SendMessageCommand.Out(entity = saved)
    }
}
