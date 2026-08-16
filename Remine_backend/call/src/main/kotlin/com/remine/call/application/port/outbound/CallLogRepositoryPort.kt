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
}
