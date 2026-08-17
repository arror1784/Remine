package com.remine.call.application.port.outbound

import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStats
import java.time.Instant
import java.util.UUID

interface CallLogRepositoryPort {
    fun save(callLog: CallLog): CallLog
    fun findById(id: UUID): CallLog?
    fun findHistoryByUserId(userId: UUID, limit: Int): List<CallLog>
    fun getCallStats(userId: UUID, since: Instant): CallStats

    // The most recent call log for this user that hasn't reached a terminal
    // state (ENDED/MISSED) yet — CONNECTING or CONNECTED. Both the caller and
    // the callee poll this to learn about ring/answer/hangup transitions the
    // other participant made, since there's no push/websocket channel here.
    fun findActiveByUserId(userId: UUID): CallLog?
}
