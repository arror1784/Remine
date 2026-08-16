package com.remine.call.application.port.inbound

import com.remine.call.domain.CallLog
import java.util.UUID

interface GetCallHistoryQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
        val limit: Int = 20,
    )

    data class Out(
        val items: List<CallLog>,
    )
}
