package com.remine.call.domain

import java.time.Instant
import java.util.UUID

data class CallLog(
    val id: UUID = UUID.randomUUID(),
    val callerId: UUID,
    val calleeId: UUID,
    val status: CallStatus = CallStatus.CONNECTING,
    val startedAt: Instant = Instant.now(),
    val endedAt: Instant? = null,
    val durationSeconds: Int = 0,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)
