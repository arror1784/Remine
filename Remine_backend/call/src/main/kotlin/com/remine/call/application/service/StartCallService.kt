package com.remine.call.application.service

import com.remine.call.application.port.inbound.StartCallCommand
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStatus
import com.remine.common.domain.exception.ForbiddenException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class StartCallService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : StartCallCommand {

    override fun handle(command: StartCallCommand.In): StartCallCommand.Out {
        // A call log is written into both participants' history, so an unchecked
        // client-supplied calleeId would let any caller plant records in a
        // stranger's call history and stats.
        if (command.calleeId != command.counterpartUserId) {
            throw ForbiddenException("You can only start a call with your paired counterpart")
        }

        val callLog = CallLog(
            callerId = command.callerId,
            calleeId = command.calleeId,
            status = CallStatus.CONNECTING,
            startedAt = Instant.now(),
        )
        val saved = callLogRepositoryPort.save(callLog)
        return StartCallCommand.Out(entity = saved)
    }
}
