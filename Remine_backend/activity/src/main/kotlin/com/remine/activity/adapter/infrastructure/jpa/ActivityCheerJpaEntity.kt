package com.remine.activity.adapter.infrastructure.jpa

import com.remine.activity.domain.ActivityCheer
import com.remine.common.persistence.BaseOrmEntity
import org.hibernate.annotations.Where
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "activity_cheer")
@Where(clause = "deleted_at IS NULL")
class ActivityCheerJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "checklist_item_id", columnDefinition = "uuid", nullable = false)
    var checklistItemId: UUID,

    @Column(name = "sender_user_id", columnDefinition = "uuid", nullable = false)
    var senderUserId: UUID,

    @Column(name = "sent_at", nullable = false)
    var sentAt: Instant,
) : BaseOrmEntity(id) {

    fun toDomain(): ActivityCheer = ActivityCheer(
        id = id,
        checklistItemId = checklistItemId,
        senderUserId = senderUserId,
        sentAt = sentAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )

    companion object {
        fun fromDomain(domain: ActivityCheer): ActivityCheerJpaEntity =
            ActivityCheerJpaEntity(
                id = domain.id,
                checklistItemId = domain.checklistItemId,
                senderUserId = domain.senderUserId,
                sentAt = domain.sentAt,
            ).apply {
                if (domain.deletedAt != null) {
                    softDelete()
                }
            }
    }
}
