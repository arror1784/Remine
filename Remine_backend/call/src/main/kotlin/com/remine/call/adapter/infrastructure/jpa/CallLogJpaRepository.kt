package com.remine.call.adapter.infrastructure.jpa

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface CallLogJpaRepository : JpaRepository<CallLogJpaEntity, UUID> {

    @Query(
        "SELECT c FROM CallLogJpaEntity c WHERE (c.callerId = :userId OR c.calleeId = :userId) ORDER BY c.startedAt DESC"
    )
    fun findHistoryByUserId(
        @Param("userId") userId: UUID,
        pageable: Pageable,
    ): List<CallLogJpaEntity>

    @Query(
        """
        SELECT COUNT(c), SUM(c.durationSeconds)
        FROM CallLogJpaEntity c
        WHERE (c.callerId = :userId OR c.calleeId = :userId)
          AND c.status = 'ENDED'
          AND c.startedAt >= :since
        """
    )
    fun getCallStats(
        @Param("userId") userId: UUID,
        @Param("since") since: Instant,
    ): List<Array<Any>>

    @Query(
        """
        SELECT c FROM CallLogJpaEntity c
        WHERE (c.callerId = :userId OR c.calleeId = :userId)
          AND c.status IN ('CONNECTING', 'CONNECTED')
        ORDER BY c.startedAt DESC
        """
    )
    fun findActiveByUserId(
        @Param("userId") userId: UUID,
        pageable: Pageable,
    ): List<CallLogJpaEntity>
}
