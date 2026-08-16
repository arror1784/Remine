package com.remine.family.adapter.infrastructure.jpa

import com.remine.family.application.port.outbound.FamilyPostReplyRepositoryPort
import com.remine.family.domain.FamilyPostReply
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class FamilyPostReplyRepositoryAdapter(
    private val jpaRepository: FamilyPostReplyJpaRepository,
) : FamilyPostReplyRepositoryPort {

    override fun save(reply: FamilyPostReply): FamilyPostReply {
        val entity = FamilyPostReplyJpaEntity.fromDomain(reply)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findByPostIdOrderByCreatedAtAsc(postId: UUID): List<FamilyPostReply> {
        return jpaRepository.findByPostIdOrderByCreatedAtAsc(postId).map { it.toDomain() }
    }

    override fun countRepliesByPostIds(postIds: Collection<UUID>): Map<UUID, Int> {
        if (postIds.isEmpty()) {
            return emptyMap()
        }
        return jpaRepository.countRepliesByPostIds(postIds)
            .associate { it.getPostId() to it.getCount().toInt() }
    }
}
