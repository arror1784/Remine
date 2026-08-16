package com.remine.message.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.message.domain.QuickReply
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "quick_reply")
@Where(clause = "deleted_at IS NULL")
class QuickReplyJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "role", length = 10, nullable = false)
    var role: String,

    @Column(name = "label", length = 100, nullable = false)
    var label: String,

    @Column(name = "sort_order", nullable = false)
    var sortOrder: Int = 0,
) : BaseOrmEntity(id) {

    fun toDomain(): QuickReply = QuickReply(
        id = id,
        role = role,
        label = label,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: QuickReply): QuickReplyJpaEntity = QuickReplyJpaEntity(
            id = domain.id,
            role = domain.role,
            label = domain.label,
            sortOrder = domain.sortOrder,
        )
    }
}
