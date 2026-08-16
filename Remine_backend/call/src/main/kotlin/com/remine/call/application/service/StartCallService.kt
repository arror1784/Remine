package com.remine.call.application.service

import com.remine.call.application.port.inbound.StartCallCommand
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class StartCallService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : StartCallCommand {

    override fun handle(command: StartCallCommand.In): StartCallCommand.Out {
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
