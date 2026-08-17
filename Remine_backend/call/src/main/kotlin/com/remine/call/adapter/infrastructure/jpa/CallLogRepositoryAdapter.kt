package com.remine.call.adapter.infrastructure.jpa

import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStats
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class CallLogRepositoryAdapter(
    private val callLogJpaRepository: CallLogJpaRepository,
) : CallLogRepositoryPort {

    override fun save(callLog: CallLog): CallLog {
        val existing = callLogJpaRepository.findById(callLog.id).orElse(null)
        val entity = if (existing != null) {
            existing.callerId = callLog.callerId
            existing.calleeId = callLog.calleeId
            existing.status = callLog.status.name
            existing.startedAt = callLog.startedAt
            existing.endedAt = callLog.endedAt
            existing.durationSeconds = callLog.durationSeconds
            existing
        } else {
            CallLogJpaEntity.fromDomain(callLog)
        }
        val saved = callLogJpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): CallLog? {
        return callLogJpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findHistoryByUserId(userId: UUID, limit: Int): List<CallLog> {
        val pageable = PageRequest.of(0, limit.coerceAtLeast(1))
        return callLogJpaRepository.findHistoryByUserId(userId, pageable).map { it.toDomain() }
    }

    override fun getCallStats(userId: UUID, since: Instant): CallStats {
        val rows = callLogJpaRepository.getCallStats(userId, since)
        val row = rows.firstOrNull()
        val count = (row?.getOrNull(0) as? Number)?.toInt() ?: 0
        val duration = (row?.getOrNull(1) as? Number)?.toLong() ?: 0L
        return CallStats(count = count, totalDurationSeconds = duration)
    }

    override fun findActiveByUserId(userId: UUID): CallLog? {
        return callLogJpaRepository.findActiveByUserId(userId, PageRequest.of(0, 1))
            .firstOrNull()
            ?.toDomain()
    }
}
