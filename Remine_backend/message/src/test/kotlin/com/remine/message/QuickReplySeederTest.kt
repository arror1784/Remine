package com.remine.message

import com.remine.message.adapter.infrastructure.seed.QuickReplySeeder
import com.remine.message.application.port.outbound.QuickReplyRepositoryPort
import com.remine.message.domain.QuickReply
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QuickReplySeederTest {

    private val savedList = mutableListOf<QuickReply>()

    private val fakeRepo = object : QuickReplyRepositoryPort {
        override fun findByRole(role: String): List<QuickReply> = savedList.filter { it.role == role }

        override fun existsByRole(role: String): Boolean = savedList.any { it.role == role }

        override fun save(quickReply: QuickReply): QuickReply {
            savedList.add(quickReply)
            return quickReply
        }

        override fun saveAll(quickReplies: List<QuickReply>): List<QuickReply> {
            savedList.addAll(quickReplies)
            return quickReplies
        }
    }

    private val seeder = QuickReplySeeder(fakeRepo)

    @Test
    fun `should seed parent and child quick replies when empty`() {
        seeder.run(null)

        val parentReplies = savedList.filter { it.role == "PARENT" }
        val childReplies = savedList.filter { it.role == "CHILD" }

        assertEquals(5, parentReplies.size)
        assertEquals("잘 있어💕", parentReplies[0].label)
        assertEquals("알겠어~", parentReplies[1].label)
        assertEquals("우리 딸 고마워 ❤️", parentReplies[2].label)
        assertEquals("보고싶다", parentReplies[3].label)
        assertEquals("전화할게", parentReplies[4].label)

        assertEquals(3, childReplies.size)
        assertEquals("잘 지내고 계세요? 💕", childReplies[0].label)
        assertEquals("오늘도 화이팅이에요!", childReplies[1].label)
        assertEquals("보고 싶어요 엄마 😊", childReplies[2].label)
    }

    @Test
    fun `should not seed when already exists`() {
        savedList.add(QuickReply(role = "PARENT", label = "기존", sortOrder = 0))
        savedList.add(QuickReply(role = "CHILD", label = "기존2", sortOrder = 0))

        seeder.run(null)

        assertEquals(2, savedList.size)
    }
}
