package com.remine.call.application.service

import com.remine.call.application.port.inbound.EndCallCommand
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallStatus
import com.remine.common.domain.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
@Transactional
class EndCallService(
    private val callLogRepositoryPort: CallLogRepositoryPort,
) : EndCallCommand {

    override fun handle(command: EndCallCommand.In): EndCallCommand.Out {
        val existing = callLogRepositoryPort.findById(command.callId)
            ?: throw EntityNotFoundException("Call log not found with id: ${command.callId}")

        val endedAt = Instant.now()
        val durationSeconds = Duration.between(existing.startedAt, endedAt).toSeconds().toInt().coerceAtLeast(0)

        val updated = existing.copy(
            status = CallStatus.ENDED,
            endedAt = endedAt,
            durationSeconds = durationSeconds,
            updatedAt = endedAt,
        )

        val saved = callLogRepositoryPort.save(updated)
        return EndCallCommand.Out(entity = saved)
    }
}
