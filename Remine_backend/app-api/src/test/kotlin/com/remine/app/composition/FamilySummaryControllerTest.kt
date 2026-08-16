package com.remine.app.composition

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.message.application.port.inbound.GetChatThreadQuery
import com.remine.message.domain.ChatMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class FamilySummaryControllerTest {

    private fun controller(
        memoryStats: GetMemoryStatsQuery.Out = GetMemoryStatsQuery.Out(totalPhotos = 0, quizActiveCount = 0, addedThisMonth = 0),
        callStats: GetCallStatsQuery.Out = GetCallStatsQuery.Out(count = 0, totalDurationSeconds = 0),
        chatItems: List<ChatMessage> = emptyList(),
    ): FamilySummaryController {
        val fakeMemoryStats = object : GetMemoryStatsQuery {
            override fun handle(query: GetMemoryStatsQuery.In) = memoryStats
        }
        val fakeCallStats = object : GetCallStatsQuery {
            override fun handle(query: GetCallStatsQuery.In) = callStats
        }
        val fakeChatThread = object : GetChatThreadQuery {
            override fun handle(query: GetChatThreadQuery.In) = GetChatThreadQuery.Out(items = chatItems)
        }
        return FamilySummaryController(fakeMemoryStats, fakeCallStats, fakeChatThread)
    }

    @Test
    fun `aggregates memory, call, and message stats for a paired parent`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)
        val chatItems = listOf(
            ChatMessage(senderId = parentId, recipientId = childId, body = "hi"),
            ChatMessage(senderId = childId, recipientId = parentId, body = "hello"),
        )

        val response = controller(
            memoryStats = GetMemoryStatsQuery.Out(totalPhotos = 12, quizActiveCount = 3, addedThisMonth = 2),
            callStats = GetCallStatsQuery.Out(count = 5, totalDurationSeconds = 600),
            chatItems = chatItems,
        ).summary(principal)

        assertEquals(12, response.data?.sharedPhotoCount)
        assertEquals(3, response.data?.quizTogetherCount)
        assertEquals(2, response.data?.messageCount)
        assertEquals(5, response.data?.callCount)
    }

    @Test
    fun `an unpaired principal gets a zero message count instead of calling the chat query`() {
        val parentId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = null)

        val response = controller(
            memoryStats = GetMemoryStatsQuery.Out(totalPhotos = 1, quizActiveCount = 0, addedThisMonth = 0),
            callStats = GetCallStatsQuery.Out(count = 0, totalDurationSeconds = 0),
        ).summary(principal)

        assertEquals(0, response.data?.messageCount)
    }

    @Test
    fun `an unpaired CHILD principal throws InvalidRequestException before any query runs`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.CHILD, pairedUserId = null)

        assertThrows<InvalidRequestException> { controller().summary(principal) }
    }
}
