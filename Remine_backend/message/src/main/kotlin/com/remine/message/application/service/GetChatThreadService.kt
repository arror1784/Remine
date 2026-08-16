package com.remine.message.application.service

import com.remine.message.application.port.inbound.GetChatThreadQuery
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetChatThreadService(
    private val chatMessageRepositoryPort: ChatMessageRepositoryPort,
) : GetChatThreadQuery {

    override fun handle(query: GetChatThreadQuery.In): GetChatThreadQuery.Out {
        val items = chatMessageRepositoryPort.findThread(
            userAId = query.userAId,
            userBId = query.userBId,
            before = query.before,
            limit = query.limit,
        )
        return GetChatThreadQuery.Out(items = items)
    }
}
