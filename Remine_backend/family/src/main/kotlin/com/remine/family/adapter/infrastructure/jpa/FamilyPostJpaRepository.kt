package com.remine.family.adapter.infrastructure.jpa

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface FamilyPostJpaRepository : JpaRepository<FamilyPostJpaEntity, UUID> {

    @Query("SELECT p FROM FamilyPostJpaEntity p WHERE p.authorUserId IN :authorUserIds ORDER BY p.createdAt DESC")
    fun findFeedWithoutCursor(
        @Param("authorUserIds") authorUserIds: Collection<UUID>,
        pageable: Pageable,
    ): List<FamilyPostJpaEntity>

    @Query("SELECT p FROM FamilyPostJpaEntity p WHERE p.authorUserId IN :authorUserIds AND p.createdAt < :cursor ORDER BY p.createdAt DESC")
    fun findFeedWithCursor(
        @Param("authorUserIds") authorUserIds: Collection<UUID>,
        @Param("cursor") cursor: Instant,
        pageable: Pageable,
    ): List<FamilyPostJpaEntity>

    fun findAllByAuthorUserId(authorUserId: UUID): List<FamilyPostJpaEntity>
}
