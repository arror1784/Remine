package com.remine.family.adapter.infrastructure.jpa

import com.remine.family.application.port.outbound.FamilyPostRepositoryPort
import com.remine.family.domain.FamilyPost
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class FamilyPostRepositoryAdapter(
    private val jpaRepository: FamilyPostJpaRepository,
) : FamilyPostRepositoryPort {

    override fun save(post: FamilyPost): FamilyPost {
        val entity = FamilyPostJpaEntity.fromDomain(post)
        val saved = jpaRepository.save(entity)
        return saved.toDomain()
    }

    override fun findById(id: UUID): FamilyPost? {
        return jpaRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findFeed(pairUserIds: Set<UUID>, cursor: Instant?, limit: Int): List<FamilyPost> {
        if (pairUserIds.isEmpty()) {
            return emptyList()
        }
        val pageable = PageRequest.of(0, limit)
        val entities = if (cursor != null) {
            jpaRepository.findFeedWithCursor(pairUserIds, cursor, pageable)
        } else {
            jpaRepository.findFeedWithoutCursor(pairUserIds, pageable)
        }
        return entities.map { it.toDomain() }
    }

    override fun existsById(id: UUID): Boolean {
        return jpaRepository.existsById(id)
    }

    override fun deleteAllByAuthorUserId(authorUserId: UUID) {
        val entities = jpaRepository.findAllByAuthorUserId(authorUserId)
        if (entities.isNotEmpty()) {
            entities.forEach { it.softDelete() }
            jpaRepository.saveAll(entities)
        }
    }
}
