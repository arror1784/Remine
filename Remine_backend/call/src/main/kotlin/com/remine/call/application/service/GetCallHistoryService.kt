package com.remine.call.application.service

import com.remine.call.application.port.inbound.GetCallHistoryQuery
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetCallHistoryService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : GetCallHistoryQuery {

    override fun handle(query: GetCallHistoryQuery.In): GetCallHistoryQuery.Out {
        val items = callLogRepositoryPort.findHistoryByUserId(query.userId, query.limit)
        return GetCallHistoryQuery.Out(items = items)
    }
}
