package com.remine.call.application.port.inbound

import com.remine.call.domain.CallLog
import java.util.UUID

interface AnswerCallCommand {
    fun handle(command: In): Out

    data class In(
        val callId: UUID,
        val answeredByUserId: UUID,
    )

    data class Out(
        val entity: CallLog,
    )
}
