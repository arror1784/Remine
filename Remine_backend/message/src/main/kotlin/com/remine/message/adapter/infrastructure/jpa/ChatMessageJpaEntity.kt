package com.remine.message.adapter.infrastructure.jpa

import com.remine.common.persistence.BaseOrmEntity
import com.remine.message.domain.ChatMessage
import org.hibernate.annotations.Where
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

@Entity
@Table(
    name = "chat_message",
    indexes = [
        Index(name = "ix_chat_message_sender", columnList = "sender_id"),
        Index(name = "ix_chat_message_recipient", columnList = "recipient_id"),
    ],
)
@Where(clause = "deleted_at IS NULL")
class ChatMessageJpaEntity(
    id: UUID = UUID.randomUUID(),

    @Column(name = "sender_id", columnDefinition = "uuid", nullable = false)
    var senderId: UUID,

    @Column(name = "recipient_id", columnDefinition = "uuid", nullable = false)
    var recipientId: UUID,

    @Column(name = "body", length = 1000, nullable = false)
    var body: String,

    @Column(name = "quick_reply_key", length = 50)
    var quickReplyKey: String? = null,
) : BaseOrmEntity(id) {

    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        senderId = senderId,
        recipientId = recipientId,
        body = body,
        quickReplyKey = quickReplyKey,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun fromDomain(domain: ChatMessage): ChatMessageJpaEntity = ChatMessageJpaEntity(
            id = domain.id,
            senderId = domain.senderId,
            recipientId = domain.recipientId,
            body = domain.body,
            quickReplyKey = domain.quickReplyKey,
        )
    }
}
