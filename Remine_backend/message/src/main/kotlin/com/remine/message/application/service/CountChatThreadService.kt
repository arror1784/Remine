package com.remine.message.application.service

import com.remine.message.application.port.inbound.CountChatThreadQuery
import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CountChatThreadService(
    private val chatMessageRepositoryPort: ChatMessageRepositoryPort,
) : CountChatThreadQuery {

    override fun handle(query: CountChatThreadQuery.In): CountChatThreadQuery.Out {
        val count = chatMessageRepositoryPort.countByPair(
            userAId = query.userAId,
            userBId = query.userBId,
        )
        return CountChatThreadQuery.Out(count = count)
    }
}
