package com.remine.message.application.service

import com.remine.message.application.port.inbound.GetQuickRepliesQuery
import com.remine.message.application.port.outbound.QuickReplyRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetQuickRepliesService(
    private val quickReplyRepositoryPort: QuickReplyRepositoryPort,
) : GetQuickRepliesQuery {

    override fun handle(query: GetQuickRepliesQuery.In): GetQuickRepliesQuery.Out {
        val items = quickReplyRepositoryPort.findByRole(query.role)
        return GetQuickRepliesQuery.Out(items = items)
    }
}
