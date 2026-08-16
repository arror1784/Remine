package com.remine.family.application.port.outbound

import com.remine.family.domain.FamilyPostLike
import java.util.UUID

interface FamilyPostLikeRepositoryPort {
    fun save(like: FamilyPostLike): FamilyPostLike
    fun findByPostIdAndUserId(postId: UUID, userId: UUID): FamilyPostLike?
    fun delete(like: FamilyPostLike)
    fun findLikedPostIds(postIds: Collection<UUID>, userId: UUID): Set<UUID>
}
