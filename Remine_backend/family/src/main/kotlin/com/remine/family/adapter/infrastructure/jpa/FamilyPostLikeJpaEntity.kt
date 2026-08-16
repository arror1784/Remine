package com.remine.family.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.family.domain.FamilyPostLike
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

@Entity
@Table(
    name = "family_post_like",
    indexes = [
        Index(name = "ix_family_post_like_post_user", columnList = "post_id, user_id")
    ]
)
@Where(clause = "deleted_at IS NULL")
class FamilyPostLikeJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "post_id", columnDefinition = "uuid", nullable = false)
    val postId: UUID,

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,
) : BaseOrmEntity(id) {

    fun toDomain(): FamilyPostLike = FamilyPostLike(
        id = id,
        postId = postId,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: FamilyPostLike): FamilyPostLikeJpaEntity = FamilyPostLikeJpaEntity(
            id = domain.id,
            postId = domain.postId,
            userId = domain.userId,
        )
    }
}
