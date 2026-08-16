package com.remine.message.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.message.application.port.inbound.GetChatThreadQuery
import com.remine.message.application.port.inbound.GetQuickRepliesQuery
import com.remine.message.application.port.inbound.SendMessageCommand
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/messages")
class MessageController(
    private val sendMessageCommand: SendMessageCommand,
    private val getChatThreadQuery: GetChatThreadQuery,
    private val getQuickRepliesQuery: GetQuickRepliesQuery,
) {

    @PostMapping
    fun sendMessage(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: SendMessageRequest,
    ): ApiResponse<ChatMessageResponse> {
        val recipientId = resolveRecipientId(principal)
        val out = sendMessageCommand.handle(
            SendMessageCommand.In(
                senderId = principal.userId,
                recipientId = recipientId,
                body = request.text,
                quickReplyKey = request.quickReplyKey,
            ),
        )
        return ApiResponse.ok(ChatMessageResponse.from(out.entity))
    }

    @GetMapping("/thread")
    fun getChatThread(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) before: Instant?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): ApiResponse<List<ChatMessageResponse>> {
        val otherUserId = resolveRecipientId(principal)
        val out = getChatThreadQuery.handle(
            GetChatThreadQuery.In(
                userAId = principal.userId,
                userBId = otherUserId,
                before = before,
                limit = limit,
            ),
        )
        return ApiResponse.ok(out.items.map { ChatMessageResponse.from(it) })
    }

    @GetMapping("/quick-replies")
    fun getQuickReplies(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<List<QuickReplyResponse>> {
        val out = getQuickRepliesQuery.handle(
            GetQuickRepliesQuery.In(
                role = principal.role.name,
            ),
        )
        return ApiResponse.ok(out.items.map { QuickReplyResponse.from(it) })
    }

    private fun resolveRecipientId(principal: RemineUserPrincipal): UUID =
        principal.requireCounterpartUserId()
}
