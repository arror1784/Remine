package com.remine.message.adapter.infrastructure.jpa

import com.remine.message.application.port.outbound.QuickReplyRepositoryPort
import com.remine.message.domain.QuickReply
import org.springframework.stereotype.Component

@Component
class QuickReplyRepositoryAdapter(
    private val jpaRepository: QuickReplyJpaRepository,
) : QuickReplyRepositoryPort {

    override fun findByRole(role: String): List<QuickReply> {
        return jpaRepository.findAllByRoleOrderBySortOrderAsc(role).map { it.toDomain() }
    }

    override fun existsByRole(role: String): Boolean {
        return jpaRepository.existsByRole(role)
    }

    override fun save(quickReply: QuickReply): QuickReply {
        val entity = QuickReplyJpaEntity.fromDomain(quickReply)
        return jpaRepository.save(entity).toDomain()
    }

    override fun saveAll(quickReplies: List<QuickReply>): List<QuickReply> {
        val entities = quickReplies.map { QuickReplyJpaEntity.fromDomain(it) }
        return jpaRepository.saveAll(entities).map { it.toDomain() }
    }
}
