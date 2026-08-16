package com.remine.notification.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.notification.domain.Notification
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "notification")
@Where(clause = "deleted_at IS NULL")
class NotificationJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "recipient_user_id", columnDefinition = "uuid", nullable = false)
    val recipientUserId: UUID,

    @Column(name = "emoji", length = 8, nullable = false)
    var emoji: String,

    @Column(name = "bg_color", length = 16, nullable = false)
    var bgColor: String,

    @Column(name = "title", length = 200, nullable = false)
    var title: String,

    @Column(name = "description", length = 500, nullable = false)
    var description: String,

    @Column(name = "deep_link", length = 255, nullable = false)
    var deepLink: String,

    @Column(name = "read", nullable = false)
    var read: Boolean = false,
) : BaseOrmEntity(id = id) {

    fun markAsRead() {
        this.read = true
    }

    fun toDomain(): Notification = Notification(
        id = id,
        recipientUserId = recipientUserId,
        emoji = emoji,
        bgColor = bgColor,
        title = title,
        description = description,
        deepLink = deepLink,
        read = read,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: Notification): NotificationJpaEntity = NotificationJpaEntity(
            id = domain.id,
            recipientUserId = domain.recipientUserId,
            emoji = domain.emoji,
            bgColor = domain.bgColor,
            title = domain.title,
            description = domain.description,
            deepLink = domain.deepLink,
            read = domain.read,
        )
    }
}
