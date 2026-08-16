package com.remine.notification.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.notification.application.port.inbound.GetNotificationsQuery
import com.remine.notification.application.port.inbound.GetUnreadNotificationCountQuery
import com.remine.notification.application.port.inbound.MarkNotificationAsReadCommand
import com.remine.notification.domain.Notification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationControllerTest {

    @Test
    fun `getNotifications returns list of notification responses wrapped in ApiResponse`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = null)
        val notif = Notification(
            recipientUserId = userId,
            emoji = "🌸",
            bgColor = "#fff7cc",
            title = "딸 지영님이 사진을 추가했어요",
            description = "2019년 봄 나들이 사진이 업로드됐어요.",
            deepLink = "/parent/memories",
        )

        val getNotificationsQuery = object : GetNotificationsQuery {
            override fun handle(query: GetNotificationsQuery.In): GetNotificationsQuery.Out {
                assertEquals(userId, query.recipientUserId)
                return GetNotificationsQuery.Out(items = listOf(notif))
            }
        }
        val markNotificationAsReadCommand = object : MarkNotificationAsReadCommand {
            override fun handle(command: MarkNotificationAsReadCommand.In): MarkNotificationAsReadCommand.Out =
                throw UnsupportedOperationException()
        }
        val getUnreadNotificationCountQuery = object : GetUnreadNotificationCountQuery {
            override fun handle(query: GetUnreadNotificationCountQuery.In): GetUnreadNotificationCountQuery.Out =
                throw UnsupportedOperationException()
        }

        val controller = NotificationController(
            getNotificationsQuery = getNotificationsQuery,
            markNotificationAsReadCommand = markNotificationAsReadCommand,
            getUnreadNotificationCountQuery = getUnreadNotificationCountQuery,
        )

        val response = controller.getNotifications(principal)

        assertNotNull(response.data)
        assertNull(response.error)
        assertEquals(1, response.data!!.size)
        assertEquals(notif.id, response.data!![0].id)
        assertEquals(notif.title, response.data!![0].title)
        assertEquals(notif.emoji, response.data!![0].emoji)
        assertEquals(notif.bgColor, response.data!![0].bgColor)
    }

    @Test
    fun `markAsRead marks notification and returns updated response`() {
        val userId = UUID.randomUUID()
        val notifId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.CHILD, pairedUserId = null)
        val readNotif = Notification(
            id = notifId,
            recipientUserId = userId,
            emoji = "⚠️",
            bgColor = "#e8f8ff",
            title = "어머니 외출이 평소보다 적어요",
            description = "오늘 아직 외출 기록이 없어요.",
            deepLink = "/child/today",
            read = true,
        )

        val getNotificationsQuery = object : GetNotificationsQuery {
            override fun handle(query: GetNotificationsQuery.In): GetNotificationsQuery.Out =
                throw UnsupportedOperationException()
        }
        val markNotificationAsReadCommand = object : MarkNotificationAsReadCommand {
            override fun handle(command: MarkNotificationAsReadCommand.In): MarkNotificationAsReadCommand.Out {
                assertEquals(notifId, command.notificationId)
                assertEquals(userId, command.recipientUserId)
                return MarkNotificationAsReadCommand.Out(entity = readNotif)
            }
        }
        val getUnreadNotificationCountQuery = object : GetUnreadNotificationCountQuery {
            override fun handle(query: GetUnreadNotificationCountQuery.In): GetUnreadNotificationCountQuery.Out =
                throw UnsupportedOperationException()
        }

        val controller = NotificationController(
            getNotificationsQuery = getNotificationsQuery,
            markNotificationAsReadCommand = markNotificationAsReadCommand,
            getUnreadNotificationCountQuery = getUnreadNotificationCountQuery,
        )

        val response = controller.markAsRead(principal, notifId)

        assertNotNull(response.data)
        assertNull(response.error)
        assertEquals(notifId, response.data!!.id)
        assertEquals(true, response.data!!.read)
    }

    @Test
    fun `getUnreadCount returns unread count wrapped in ApiResponse`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = null)

        val getNotificationsQuery = object : GetNotificationsQuery {
            override fun handle(query: GetNotificationsQuery.In): GetNotificationsQuery.Out =
                throw UnsupportedOperationException()
        }
        val markNotificationAsReadCommand = object : MarkNotificationAsReadCommand {
            override fun handle(command: MarkNotificationAsReadCommand.In): MarkNotificationAsReadCommand.Out =
                throw UnsupportedOperationException()
        }
        val getUnreadNotificationCountQuery = object : GetUnreadNotificationCountQuery {
            override fun handle(query: GetUnreadNotificationCountQuery.In): GetUnreadNotificationCountQuery.Out {
                assertEquals(userId, query.recipientUserId)
                return GetUnreadNotificationCountQuery.Out(count = 4)
            }
        }

        val controller = NotificationController(
            getNotificationsQuery = getNotificationsQuery,
            markNotificationAsReadCommand = markNotificationAsReadCommand,
            getUnreadNotificationCountQuery = getUnreadNotificationCountQuery,
        )

        val response = controller.getUnreadCount(principal)

        assertNotNull(response.data)
        assertNull(response.error)
        assertEquals(4, response.data!!.count)
    }
}
