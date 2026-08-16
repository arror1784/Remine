package com.remine.notification.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.notification.application.port.inbound.GetNotificationsQuery
import com.remine.notification.application.port.inbound.GetUnreadNotificationCountQuery
import com.remine.notification.application.port.inbound.MarkNotificationAsReadCommand
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val getNotificationsQuery: GetNotificationsQuery,
    private val markNotificationAsReadCommand: MarkNotificationAsReadCommand,
    private val getUnreadNotificationCountQuery: GetUnreadNotificationCountQuery,
) {

    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<List<NotificationResponse>> {
        val result = getNotificationsQuery.handle(
            GetNotificationsQuery.In(recipientUserId = principal.userId)
        )
        return ApiResponse.ok(result.items.map { NotificationResponse.from(it) })
    }

    @PatchMapping("/{id}/read")
    fun markAsRead(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable("id") id: UUID,
    ): ApiResponse<NotificationResponse> {
        val result = markNotificationAsReadCommand.handle(
            MarkNotificationAsReadCommand.In(
                notificationId = id,
                recipientUserId = principal.userId,
            )
        )
        return ApiResponse.ok(NotificationResponse.from(result.entity))
    }

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<UnreadNotificationCountResponse> {
        val result = getUnreadNotificationCountQuery.handle(
            GetUnreadNotificationCountQuery.In(recipientUserId = principal.userId)
        )
        return ApiResponse.ok(UnreadNotificationCountResponse(count = result.count))
    }
}
