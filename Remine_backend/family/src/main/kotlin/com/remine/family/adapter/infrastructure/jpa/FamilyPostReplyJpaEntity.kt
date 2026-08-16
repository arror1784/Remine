package com.remine.family.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.family.domain.FamilyPostReply
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

@Entity
@Table(
    name = "family_post_reply",
    indexes = [
        Index(name = "ix_family_post_reply_post", columnList = "post_id")
    ]
)
@Where(clause = "deleted_at IS NULL")
class FamilyPostReplyJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "post_id", columnDefinition = "uuid", nullable = false)
    val postId: UUID,

    @Column(name = "author_user_id", columnDefinition = "uuid", nullable = false)
    val authorUserId: UUID,

    @Column(name = "body", length = 500, nullable = false)
    var body: String,
) : BaseOrmEntity(id) {

    fun toDomain(): FamilyPostReply = FamilyPostReply(
        id = id,
        postId = postId,
        authorUserId = authorUserId,
        body = body,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: FamilyPostReply): FamilyPostReplyJpaEntity = FamilyPostReplyJpaEntity(
            id = domain.id,
            postId = domain.postId,
            authorUserId = domain.authorUserId,
            body = domain.body,
        )
    }
}
