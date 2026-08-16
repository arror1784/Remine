package com.remine.call.adapter.infrastructure.jpa

import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStatus
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "call_log")
@Where(clause = "deleted_at IS NULL")
class CallLogJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "caller_id", columnDefinition = "uuid", nullable = false)
    var callerId: UUID,

    @Column(name = "callee_id", columnDefinition = "uuid", nullable = false)
    var calleeId: UUID,

    @Column(name = "status", length = 20, nullable = false)
    var status: String,

    @Column(name = "started_at", nullable = false)
    var startedAt: Instant,

    @Column(name = "ended_at")
    var endedAt: Instant? = null,

    @Column(name = "duration_seconds", nullable = false)
    var durationSeconds: Int = 0,
) : BaseOrmEntity(id = id) {

    fun toDomain(): CallLog = CallLog(
        id = id,
        callerId = callerId,
        calleeId = calleeId,
        status = CallStatus.from(status),
        startedAt = startedAt,
        endedAt = endedAt,
        durationSeconds = durationSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: CallLog): CallLogJpaEntity = CallLogJpaEntity(
            id = domain.id,
            callerId = domain.callerId,
            calleeId = domain.calleeId,
            status = domain.status.name,
            startedAt = domain.startedAt,
            endedAt = domain.endedAt,
            durationSeconds = domain.durationSeconds,
        )
    }
}
