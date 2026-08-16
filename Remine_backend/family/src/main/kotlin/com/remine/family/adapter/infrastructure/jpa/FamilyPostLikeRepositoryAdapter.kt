package com.remine.family.adapter.infrastructure.jpa

import com.remine.family.application.port.outbound.FamilyPostLikeRepositoryPort
import com.remine.family.domain.FamilyPostLike
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FamilyPostLikeRepositoryAdapter(
    private val jpaRepository: FamilyPostLikeJpaRepository,
) : FamilyPostLikeRepositoryPort {

    override fun save(like: FamilyPostLike): FamilyPostLike {
        val entity = FamilyPostLikeJpaEntity.fromDomain(like)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByPostIdAndUserId(postId: UUID, userId: UUID): FamilyPostLike? {
        return jpaRepository.findByPostIdAndUserId(postId, userId)?.toDomain()
    }

    override fun delete(like: FamilyPostLike) {
        val entity = jpaRepository.findById(like.id).orElse(null)
        if (entity != null) {
            entity.softDelete()
            jpaRepository.save(entity)
        }
    }

    override fun findLikedPostIds(postIds: Collection<UUID>, userId: UUID): Set<UUID> {
        if (postIds.isEmpty()) {
            return emptySet()
        }
        return jpaRepository.findLikedPostIds(postIds, userId).toSet()
    }
}
