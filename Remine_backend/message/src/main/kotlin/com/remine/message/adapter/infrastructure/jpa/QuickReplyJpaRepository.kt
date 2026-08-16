package com.remine.message.adapter.infrastructure.jpa

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface QuickReplyJpaRepository : JpaRepository<QuickReplyJpaEntity, UUID> {
    fun findAllByRoleOrderBySortOrderAsc(role: String): List<QuickReplyJpaEntity>
    fun existsByRole(role: String): Boolean
}
