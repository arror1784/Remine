package com.remine.message

import com.remine.message.application.port.inbound.GetQuickRepliesQuery
import com.remine.message.application.port.outbound.QuickReplyRepositoryPort
import com.remine.message.application.service.GetQuickRepliesService
import com.remine.message.domain.QuickReply
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetQuickRepliesServiceTest {

    private val fakeRepo = object : QuickReplyRepositoryPort {
        override fun findByRole(role: String): List<QuickReply> {
            return if (role == "PARENT") {
                listOf(
                    QuickReply(role = "PARENT", label = "잘 있어💕", sortOrder = 0),
                    QuickReply(role = "PARENT", label = "알겠어~", sortOrder = 1),
                )
            } else {
                emptyList()
            }
        }

        override fun existsByRole(role: String): Boolean = role == "PARENT"
        override fun save(quickReply: QuickReply): QuickReply = quickReply
        override fun saveAll(quickReplies: List<QuickReply>): List<QuickReply> = quickReplies
    }

    private val service = GetQuickRepliesService(fakeRepo)

    @Test
    fun `should return quick replies by role`() {
        val result = service.handle(GetQuickRepliesQuery.In(role = "PARENT"))
        assertEquals(2, result.items.size)
        assertEquals("잘 있어💕", result.items[0].label)
        assertEquals("알겠어~", result.items[1].label)
    }
}
