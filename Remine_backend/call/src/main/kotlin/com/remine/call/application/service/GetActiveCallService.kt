package com.remine.call.application.service

import com.remine.call.application.port.inbound.GetActiveCallQuery
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetActiveCallService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : GetActiveCallQuery {

    override fun handle(query: GetActiveCallQuery.In): GetActiveCallQuery.Out {
        val call = callLogRepositoryPort.findActiveByUserId(query.userId)
        return GetActiveCallQuery.Out(call = call)
    }
}
