package com.remine.family.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FamilyPostLikeJpaRepository : JpaRepository<FamilyPostLikeJpaEntity, UUID> {

    fun findByPostIdAndUserId(postId: UUID, userId: UUID): FamilyPostLikeJpaEntity?

    fun existsByPostIdAndUserId(postId: UUID, userId: UUID): Boolean

    @Query("SELECT l.postId FROM FamilyPostLikeJpaEntity l WHERE l.postId IN :postIds AND l.userId = :userId")
    fun findLikedPostIds(
        @Param("postIds") postIds: Collection<UUID>,
        @Param("userId") userId: UUID,
    ): List<UUID>
}
