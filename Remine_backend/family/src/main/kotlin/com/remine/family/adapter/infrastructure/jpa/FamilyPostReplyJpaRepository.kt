package com.remine.family.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FamilyPostReplyJpaRepository : JpaRepository<FamilyPostReplyJpaEntity, UUID> {

    fun findByPostIdOrderByCreatedAtAsc(postId: UUID): List<FamilyPostReplyJpaEntity>

    fun countByPostId(postId: UUID): Int

    @Query("SELECT r.postId as postId, COUNT(r.id) as count FROM FamilyPostReplyJpaEntity r WHERE r.postId IN :postIds GROUP BY r.postId")
    fun countRepliesByPostIds(
        @Param("postIds") postIds: Collection<UUID>,
    ): List<PostReplyCountProjection>
}
