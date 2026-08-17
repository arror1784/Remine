package com.remine.call.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.call.application.port.inbound.AnswerCallCommand
import com.remine.call.application.port.inbound.EndCallCommand
import com.remine.call.application.port.inbound.GetActiveCallQuery
import com.remine.call.application.port.inbound.GetCallHistoryQuery
import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.call.application.port.inbound.StartCallCommand
import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class CallControllerTest {

    private val sampleCallLog = CallLog(
        id = UUID.randomUUID(),
        callerId = UUID.randomUUID(),
        calleeId = UUID.randomUUID(),
        status = CallStatus.CONNECTING,
        startedAt = Instant.now(),
    )

    private val mockStartCallCommand = object : StartCallCommand {
        override fun handle(command: StartCallCommand.In): StartCallCommand.Out {
            return StartCallCommand.Out(
                entity = sampleCallLog.copy(callerId = command.callerId, calleeId = command.calleeId)
            )
        }
    }

    private val mockAnswerCallCommand = object : AnswerCallCommand {
        override fun handle(command: AnswerCallCommand.In): AnswerCallCommand.Out {
            return AnswerCallCommand.Out(
                entity = sampleCallLog.copy(id = command.callId, status = CallStatus.CONNECTED)
            )
        }
    }

    private val mockEndCallCommand = object : EndCallCommand {
        override fun handle(command: EndCallCommand.In): EndCallCommand.Out {
            return EndCallCommand.Out(
                entity = sampleCallLog.copy(
                    id = command.callId,
                    status = CallStatus.ENDED,
                    endedAt = Instant.now(),
                    durationSeconds = 42,
                )
            )
        }
    }

    private val mockGetCallHistoryQuery = object : GetCallHistoryQuery {
        override fun handle(query: GetCallHistoryQuery.In): GetCallHistoryQuery.Out {
            return GetCallHistoryQuery.Out(
                items = listOf(sampleCallLog.copy(callerId = query.userId))
            )
        }
    }

    private val mockGetCallStatsQuery = object : GetCallStatsQuery {
        override fun handle(query: GetCallStatsQuery.In): GetCallStatsQuery.Out {
            return GetCallStatsQuery.Out(count = 5, totalDurationSeconds = 300L)
        }
    }

    private val mockGetActiveCallQuery = object : GetActiveCallQuery {
        override fun handle(query: GetActiveCallQuery.In): GetActiveCallQuery.Out {
            return GetActiveCallQuery.Out(call = sampleCallLog.copy(calleeId = query.userId))
        }
    }

    private val controller = CallController(
        startCallCommand = mockStartCallCommand,
        answerCallCommand = mockAnswerCallCommand,
        endCallCommand = mockEndCallCommand,
        getCallHistoryQuery = mockGetCallHistoryQuery,
        getCallStatsQuery = mockGetCallStatsQuery,
        getActiveCallQuery = mockGetActiveCallQuery,
    )

    @Test
    fun `start call as parent routes to counterpart child`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = childId)

        val response = controller.startCall(principal = principal, request = null)

        assertNotNull(response.data)
        assertEquals(parentId, response.data?.callerId)
        assertEquals(childId, response.data?.calleeId)
    }

    @Test
    fun `start call as child routes to parent`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = childId, role = Role.CHILD, pairedUserId = parentId)

        val response = controller.startCall(principal = principal, request = null)

        assertNotNull(response.data)
        assertEquals(childId, response.data?.callerId)
        assertEquals(parentId, response.data?.calleeId)
    }

    @Test
    fun `answer call answers the call and returns response`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.CHILD, pairedUserId = null)
        val callId = UUID.randomUUID()

        val response = controller.answerCall(id = callId, principal = principal)

        assertEquals("CONNECTED", response.data?.status)
        assertEquals(callId, response.data?.id)
    }

    @Test
    fun `get active call returns the caller's current call`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = null)

        val response = controller.getActiveCall(principal = principal)

        assertEquals(userId, response.data?.calleeId)
    }

    @Test
    fun `end call ends the call and returns response`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.PARENT, pairedUserId = null)
        val callId = UUID.randomUUID()

        val response = controller.endCall(id = callId, principal = principal)

        assertEquals("ENDED", response.data?.status)
        assertEquals(42, response.data?.durationSeconds)
    }

    @Test
    fun `get history returns calls`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.PARENT, pairedUserId = null)
        val response = controller.getCallHistory(principal = principal, limit = 10)

        assertEquals(1, response.data?.size)
    }

    @Test
    fun `get stats returns aggregated counts`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.PARENT, pairedUserId = null)
        val response = controller.getCallStats(principal = principal)

        assertEquals(5, response.data?.count)
        assertEquals(300L, response.data?.totalDurationSeconds)
    }
}
