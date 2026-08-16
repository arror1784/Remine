package com.remine.message

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.message.adapter.presentation.web.MessageController
import com.remine.message.adapter.presentation.web.SendMessageRequest
import com.remine.message.application.port.inbound.GetChatThreadQuery
import com.remine.message.application.port.inbound.GetQuickRepliesQuery
import com.remine.message.application.port.inbound.SendMessageCommand
import com.remine.message.domain.ChatMessage
import com.remine.message.domain.QuickReply
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class MessageControllerTest {

    private val fakeSendCommand = object : SendMessageCommand {
        override fun handle(command: SendMessageCommand.In): SendMessageCommand.Out {
            return SendMessageCommand.Out(
                entity = ChatMessage(
                    senderId = command.senderId,
                    recipientId = command.recipientId,
                    body = command.body,
                    quickReplyKey = command.quickReplyKey,
                )
            )
        }
    }

    private val fakeThreadQuery = object : GetChatThreadQuery {
        override fun handle(query: GetChatThreadQuery.In): GetChatThreadQuery.Out {
            return GetChatThreadQuery.Out(
                items = listOf(
                    ChatMessage(senderId = query.userAId, recipientId = query.userBId, body = "Hello"),
                )
            )
        }
    }

    private val fakeQuickReplyQuery = object : GetQuickRepliesQuery {
        override fun handle(query: GetQuickRepliesQuery.In): GetQuickRepliesQuery.Out {
            return GetQuickRepliesQuery.Out(
                items = listOf(
                    QuickReply(role = query.role, label = "테스트", sortOrder = 0),
                )
            )
        }
    }

    private val controller = MessageController(
        sendMessageCommand = fakeSendCommand,
        getChatThreadQuery = fakeThreadQuery,
        getQuickRepliesQuery = fakeQuickReplyQuery,
    )

    @Test
    fun `parent can send message to paired child`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)

        val response = controller.sendMessage(
            principal = principal,
            request = SendMessageRequest(text = "밥 먹었니?", quickReplyKey = null),
        )

        assertNotNull(response.data)
        assertEquals("밥 먹었니?", response.data?.body)
        assertEquals(parentId, response.data?.senderId)
        assertEquals(childId, response.data?.recipientId)
    }

    @Test
    fun `unpaired parent throws InvalidRequestException`() {
        val parentId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = null)

        assertThrows(InvalidRequestException::class.java) {
            controller.sendMessage(
                principal = principal,
                request = SendMessageRequest(text = "안녕"),
            )
        }
    }

    @Test
    fun `child can send message to parent`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = childId, role = Role.CHILD, pairedUserId = parentId)

        val response = controller.sendMessage(
            principal = principal,
            request = SendMessageRequest(text = "엄마 사랑해요", quickReplyKey = "quick_love"),
        )

        assertNotNull(response.data)
        assertEquals("엄마 사랑해요", response.data?.body)
        assertEquals(childId, response.data?.senderId)
        assertEquals(parentId, response.data?.recipientId)
    }

    @Test
    fun `getChatThread returns messages between pair`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)

        val response = controller.getChatThread(
            principal = principal,
            before = null,
            limit = 50,
        )

        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
        assertEquals("Hello", response.data?.first()?.body)
    }

    @Test
    fun `getQuickReplies returns role replies`() {
        val parentId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = null)

        val response = controller.getQuickReplies(principal = principal)

        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
        assertEquals("테스트", response.data?.first()?.label)
    }
}
