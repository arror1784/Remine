package com.remine.message.adapter.infrastructure.jpa

import com.remine.message.application.port.outbound.ChatMessageRepositoryPort
import com.remine.message.domain.ChatMessage
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class ChatMessageRepositoryAdapter(
    private val jpaRepository: ChatMessageJpaRepository,
) : ChatMessageRepositoryPort {

    override fun save(chatMessage: ChatMessage): ChatMessage {
        val entity = ChatMessageJpaEntity.fromDomain(chatMessage)
        return jpaRepository.save(entity).toDomain()
    }

    override fun findThread(
        userAId: UUID,
        userBId: UUID,
        before: Instant?,
        limit: Int,
    ): List<ChatMessage> {
        val pageable = PageRequest.of(0, limit)
        val entities = if (before != null) {
            jpaRepository.findThreadBefore(userAId, userBId, before, pageable)
        } else {
            jpaRepository.findThread(userAId, userBId, pageable)
        }
        return entities.map { it.toDomain() }
    }

    override fun countByPair(userAId: UUID, userBId: UUID): Int =
        jpaRepository.countThread(userAId, userBId).toInt()
}
