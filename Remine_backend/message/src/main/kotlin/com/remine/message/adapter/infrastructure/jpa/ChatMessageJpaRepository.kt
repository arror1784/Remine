package com.remine.message.adapter.infrastructure.jpa

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ChatMessageJpaRepository : JpaRepository<ChatMessageJpaEntity, UUID> {

    @Query(
        """
        SELECT m FROM ChatMessageJpaEntity m
        WHERE ( (m.senderId = :userAId AND m.recipientId = :userBId)
             OR (m.senderId = :userBId AND m.recipientId = :userAId) )
          AND m.createdAt < :before
        ORDER BY m.createdAt DESC
        """
    )
    fun findThreadBefore(
        @Param("userAId") userAId: UUID,
        @Param("userBId") userBId: UUID,
        @Param("before") before: Instant,
        pageable: Pageable,
    ): List<ChatMessageJpaEntity>

    @Query(
        """
        SELECT m FROM ChatMessageJpaEntity m
        WHERE (m.senderId = :userAId AND m.recipientId = :userBId)
           OR (m.senderId = :userBId AND m.recipientId = :userAId)
        ORDER BY m.createdAt DESC
        """
    )
    fun findThread(
        @Param("userAId") userAId: UUID,
        @Param("userBId") userBId: UUID,
        pageable: Pageable,
    ): List<ChatMessageJpaEntity>

    @Query(
        """
        SELECT COUNT(m) FROM ChatMessageJpaEntity m
        WHERE (m.senderId = :userAId AND m.recipientId = :userBId)
           OR (m.senderId = :userBId AND m.recipientId = :userAId)
        """
    )
    fun countThread(
        @Param("userAId") userAId: UUID,
        @Param("userBId") userBId: UUID,
    ): Long

    @Query("SELECT m FROM ChatMessageJpaEntity m WHERE m.senderId = :userId OR m.recipientId = :userId")
    fun findAllByParticipant(@Param("userId") userId: UUID): List<ChatMessageJpaEntity>
}
