package com.remine.call.application.service

import com.remine.call.application.port.inbound.AnswerCallCommand
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallStatus
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AnswerCallService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : AnswerCallCommand {

    override fun handle(command: AnswerCallCommand.In): AnswerCallCommand.Out {
        val existing = callLogRepositoryPort.findById(command.callId)
            ?: throw EntityNotFoundException("Call log not found with id: ${command.callId}")

        if (command.answeredByUserId != existing.calleeId) {
            throw ForbiddenException("Only the callee can answer a call")
        }

        // Already answered — return as-is instead of erroring, so a doubled
        // "수락" tap (slow network, re-render) doesn't surface as a failure.
        if (existing.status == CallStatus.CONNECTED) {
            return AnswerCallCommand.Out(entity = existing)
        }

        if (existing.status != CallStatus.CONNECTING) {
            throw InvalidRequestException("Call ${command.callId} is ${existing.status}, not CONNECTING")
        }

        val updated = existing.copy(status = CallStatus.CONNECTED)
        val saved = callLogRepositoryPort.save(updated)
        return AnswerCallCommand.Out(entity = saved)
    }
}
