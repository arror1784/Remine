package com.remine.call.application.port.inbound

import com.remine.call.domain.CallLog
import java.util.UUID

interface GetActiveCallQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
    )

    data class Out(
        val call: CallLog?,
    )
}
