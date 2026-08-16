package com.remine.call.application.port.inbound

import com.remine.call.domain.CallLog
import java.util.UUID

interface StartCallCommand {
    fun handle(command: In): Out

    data class In(
        val callerId: UUID,
        val calleeId: UUID,
        val counterpartUserId: UUID,
    )

    data class Out(
        val entity: CallLog,
    )
}
