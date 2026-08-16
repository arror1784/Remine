package com.remine.memory.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table

@Entity
@Table(name = "memory_photo")
@Where(clause = "deleted_at IS NULL")
class MemoryPhotoJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "owner_user_id", columnDefinition = "uuid", nullable = false)
    val ownerUserId: UUID,

    @Column(name = "uploaded_by_user_id", columnDefinition = "uuid", nullable = false)
    val uploadedByUserId: UUID,

    @Column(name = "title", length = 200, nullable = false)
    var title: String,

    @Column(name = "photo_url", length = 500, nullable = false)
    var photoUrl: String,

    @Column(name = "memory_label", length = 50, nullable = false)
    var memoryLabel: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: MemoryPhotoStatus = MemoryPhotoStatus.PENDING,
) : BaseOrmEntity(id) {

    fun toDomain(): MemoryPhoto =
        MemoryPhoto(
            id = id,
            ownerUserId = ownerUserId,
            uploadedByUserId = uploadedByUserId,
            title = title,
            photoUrl = photoUrl,
            memoryLabel = memoryLabel,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
        )

    fun updateFrom(domain: MemoryPhoto) {
        this.title = domain.title
        this.photoUrl = domain.photoUrl
        this.memoryLabel = domain.memoryLabel
        this.status = domain.status
    }

    companion object {
        fun from(domain: MemoryPhoto): MemoryPhotoJpaEntity {
            return MemoryPhotoJpaEntity(
                id = domain.id,
                ownerUserId = domain.ownerUserId,
                uploadedByUserId = domain.uploadedByUserId,
                title = domain.title,
                photoUrl = domain.photoUrl,
                memoryLabel = domain.memoryLabel,
                status = domain.status,
            )
        }
    }
}
