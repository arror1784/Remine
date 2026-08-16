package com.remine.family.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.family.domain.FamilyPost
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

@Entity
@Table(
    name = "family_post",
    indexes = [
        Index(name = "ix_family_post_author", columnList = "author_user_id")
    ]
)
@Where(clause = "deleted_at IS NULL")
class FamilyPostJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "author_user_id", columnDefinition = "uuid", nullable = false)
    val authorUserId: UUID,

    @Column(name = "body", length = 1000, nullable = false)
    var body: String,

    @Column(name = "photo_url", length = 500)
    var photoUrl: String? = null,

    @Column(name = "photo_caption", length = 200)
    var photoCaption: String? = null,

    @Column(name = "like_count", nullable = false)
    var likeCount: Int = 0,
) : BaseOrmEntity(id) {

    fun toDomain(): FamilyPost = FamilyPost(
        id = id,
        authorUserId = authorUserId,
        body = body,
        photoUrl = photoUrl,
        photoCaption = photoCaption,
        likeCount = likeCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: FamilyPost): FamilyPostJpaEntity = FamilyPostJpaEntity(
            id = domain.id,
            authorUserId = domain.authorUserId,
            body = domain.body,
            photoUrl = domain.photoUrl,
            photoCaption = domain.photoCaption,
            likeCount = domain.likeCount,
        )
    }
}
