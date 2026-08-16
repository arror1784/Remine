package com.remine.common.persistence

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.UUID
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 * Every module's *JpaEntity extends this. Soft-delete only — never model
 * "deleted" as a separate is_active flag (see root CLAUDE.md rule 10).
 *
 * Subclasses must declare their own `@Where(clause = "deleted_at IS NULL")`
 * (or `@SQLRestriction` on newer Hibernate) since that annotation does not
 * reliably propagate from a @MappedSuperclass to the subclass table in
 * Hibernate 5.6.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseOrmEntity(
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID = UUID.randomUUID(),
) {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
        protected set

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
        protected set

    fun softDelete() {
        deletedAt = Instant.now()
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseOrmEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
