package com.remine.call.adapter.presentation.web

import com.remine.call.domain.CallLog
import java.time.Instant
import java.util.UUID

data class CallResponse(
    val id: UUID,
    val callerId: UUID,
    val calleeId: UUID,
    val status: String,
    val startedAt: Instant,
    val endedAt: Instant?,
    val durationSeconds: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        fun from(callLog: CallLog): CallResponse = CallResponse(
            id = callLog.id,
            callerId = callLog.callerId,
            calleeId = callLog.calleeId,
            status = callLog.status.name,
            startedAt = callLog.startedAt,
            endedAt = callLog.endedAt,
            durationSeconds = callLog.durationSeconds,
            createdAt = callLog.createdAt,
            updatedAt = callLog.updatedAt,
        )
    }
}
