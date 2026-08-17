package com.remine.call.application.service

import com.remine.call.application.port.inbound.AnswerCallCommand
import com.remine.call.application.port.inbound.EndCallCommand
import com.remine.call.application.port.inbound.GetActiveCallQuery
import com.remine.call.application.port.inbound.GetCallHistoryQuery
import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.call.application.port.inbound.StartCallCommand
import com.remine.call.application.port.outbound.CallLogRepositoryPort
import com.remine.call.domain.CallLog
import com.remine.call.domain.CallStats
import com.remine.call.domain.CallStatus
import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.ForbiddenException
import com.remine.common.domain.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class CallServiceTest {

    private class MockCallLogRepositoryPort : CallLogRepositoryPort {
        val storage = mutableMapOf<UUID, CallLog>()

        override fun save(callLog: CallLog): CallLog {
            storage[callLog.id] = callLog
            return callLog
        }

        override fun findById(id: UUID): CallLog? = storage[id]

        override fun findHistoryByUserId(userId: UUID, limit: Int): List<CallLog> {
            return storage.values
                .filter { it.callerId == userId || it.calleeId == userId }
                .sortedByDescending { it.startedAt }
                .take(limit)
        }

        override fun getCallStats(userId: UUID, since: Instant): CallStats {
            val matching = storage.values.filter {
                (it.callerId == userId || it.calleeId == userId) &&
                    it.status == CallStatus.ENDED &&
                    !it.startedAt.isBefore(since)
            }
            val count = matching.size
            val totalDuration = matching.sumOf { it.durationSeconds.toLong() }
            return CallStats(count = count, totalDurationSeconds = totalDuration)
        }

        override fun findActiveByUserId(userId: UUID): CallLog? {
            return storage.values
                .filter {
                    (it.callerId == userId || it.calleeId == userId) &&
                        (it.status == CallStatus.CONNECTING || it.status == CallStatus.CONNECTED)
                }
                .maxByOrNull { it.startedAt }
        }
    }

    @Test
    fun `start call should create a CONNECTING call log`() {
        val repo = MockCallLogRepositoryPort()
        val service = StartCallService(repo)

        val callerId = UUID.randomUUID()
        val calleeId = UUID.randomUUID()

        val out = service.handle(StartCallCommand.In(callerId = callerId, calleeId = calleeId, counterpartUserId = calleeId))

        assertNotNull(out.entity.id)
        assertEquals(callerId, out.entity.callerId)
        assertEquals(calleeId, out.entity.calleeId)
        assertEquals(CallStatus.CONNECTING, out.entity.status)
        assertNotNull(out.entity.startedAt)
    }

    @Test
    fun `end call should set ENDED and calculate duration`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val endService = EndCallService(repo)

        val callerId = UUID.randomUUID()
        val calleeId = UUID.randomUUID()

        val created = startService.handle(StartCallCommand.In(callerId = callerId, calleeId = calleeId, counterpartUserId = calleeId)).entity

        val endedOut = endService.handle(EndCallCommand.In(callId = created.id, endedByUserId = callerId))

        assertEquals(CallStatus.ENDED, endedOut.entity.status)
        assertNotNull(endedOut.entity.endedAt)
        assertEquals(0, endedOut.entity.durationSeconds) // immediately ended
    }

    @Test
    fun `end call throws EntityNotFoundException when not found`() {
        val repo = MockCallLogRepositoryPort()
        val endService = EndCallService(repo)

        assertThrows(EntityNotFoundException::class.java) {
            endService.handle(EndCallCommand.In(callId = UUID.randomUUID(), endedByUserId = UUID.randomUUID()))
        }
    }

    @Test
    fun `end call throws ForbiddenException when caller is not a participant`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val endService = EndCallService(repo)

        val created = startService.handle(
            newStartCallIn(callerId = UUID.randomUUID(), calleeId = UUID.randomUUID()),
        ).entity

        assertThrows(ForbiddenException::class.java) {
            endService.handle(EndCallCommand.In(callId = created.id, endedByUserId = UUID.randomUUID()))
        }
        assertEquals(CallStatus.CONNECTING, repo.storage.getValue(created.id).status)
    }

    @Test
    fun `end call succeeds when the callee ends it`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val endService = EndCallService(repo)

        val calleeId = UUID.randomUUID()
        val created = startService.handle(
            newStartCallIn(callerId = UUID.randomUUID(), calleeId = calleeId),
        ).entity

        val endedOut = endService.handle(EndCallCommand.In(callId = created.id, endedByUserId = calleeId))

        assertEquals(CallStatus.ENDED, endedOut.entity.status)
    }

    @Test
    fun `get call history and stats query work as expected`() {
        val repo = MockCallLogRepositoryPort()
        val historyService = GetCallHistoryService(repo)
        val statsService = GetCallStatsService(repo)

        val userId = UUID.randomUUID()
        val otherId = UUID.randomUUID()

        val call1 = CallLog(
            callerId = userId,
            calleeId = otherId,
            status = CallStatus.ENDED,
            startedAt = Instant.now().minusSeconds(100),
            endedAt = Instant.now().minusSeconds(40),
            durationSeconds = 60,
        )
        val call2 = CallLog(
            callerId = otherId,
            calleeId = userId,
            status = CallStatus.ENDED,
            startedAt = Instant.now().minusSeconds(30),
            endedAt = Instant.now(),
            durationSeconds = 30,
        )
        repo.save(call1)
        repo.save(call2)

        val history = historyService.handle(GetCallHistoryQuery.In(userId = userId, limit = 10))
        assertEquals(2, history.items.size)

        val stats = statsService.handle(GetCallStatsQuery.In(userId = userId, sinceMonthStart = LocalDate.now().withDayOfMonth(1)))
        assertEquals(2, stats.count)
        assertEquals(90L, stats.totalDurationSeconds)
    }

    @Test
    fun `start call throws ForbiddenException when calleeId is not the caller's counterpart`() {
        val repo = MockCallLogRepositoryPort()
        val service = StartCallService(repo)

        val strangerId = UUID.randomUUID()

        assertThrows(ForbiddenException::class.java) {
            service.handle(
                StartCallCommand.In(
                    callerId = UUID.randomUUID(),
                    calleeId = strangerId,
                    counterpartUserId = UUID.randomUUID(),
                ),
            )
        }
        assertTrue(repo.storage.isEmpty())
    }

    @Test
    fun `answer call transitions CONNECTING to CONNECTED for the callee`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val answerService = AnswerCallService(repo)

        val calleeId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = UUID.randomUUID(), calleeId = calleeId)).entity

        val answered = answerService.handle(AnswerCallCommand.In(callId = created.id, answeredByUserId = calleeId))

        assertEquals(CallStatus.CONNECTED, answered.entity.status)
    }

    @Test
    fun `answer call throws ForbiddenException when the caller tries to answer their own call`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val answerService = AnswerCallService(repo)

        val callerId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = callerId, calleeId = UUID.randomUUID())).entity

        assertThrows(ForbiddenException::class.java) {
            answerService.handle(AnswerCallCommand.In(callId = created.id, answeredByUserId = callerId))
        }
    }

    @Test
    fun `answer call is idempotent once already CONNECTED`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val answerService = AnswerCallService(repo)

        val calleeId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = UUID.randomUUID(), calleeId = calleeId)).entity
        answerService.handle(AnswerCallCommand.In(callId = created.id, answeredByUserId = calleeId))

        val secondAnswer = answerService.handle(AnswerCallCommand.In(callId = created.id, answeredByUserId = calleeId))

        assertEquals(CallStatus.CONNECTED, secondAnswer.entity.status)
    }

    @Test
    fun `answer call throws InvalidRequestException once the call has already ended`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val endService = EndCallService(repo)
        val answerService = AnswerCallService(repo)

        val calleeId = UUID.randomUUID()
        val callerId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = callerId, calleeId = calleeId)).entity
        endService.handle(EndCallCommand.In(callId = created.id, endedByUserId = callerId))

        assertThrows(InvalidRequestException::class.java) {
            answerService.handle(AnswerCallCommand.In(callId = created.id, answeredByUserId = calleeId))
        }
    }

    @Test
    fun `get active call finds a CONNECTING or CONNECTED call for either participant`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val activeService = GetActiveCallService(repo)

        val callerId = UUID.randomUUID()
        val calleeId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = callerId, calleeId = calleeId)).entity

        val callerView = activeService.handle(GetActiveCallQuery.In(userId = callerId))
        val calleeView = activeService.handle(GetActiveCallQuery.In(userId = calleeId))

        assertEquals(created.id, callerView.call?.id)
        assertEquals(created.id, calleeView.call?.id)
    }

    @Test
    fun `get active call returns null once the call has ended`() {
        val repo = MockCallLogRepositoryPort()
        val startService = StartCallService(repo)
        val endService = EndCallService(repo)
        val activeService = GetActiveCallService(repo)

        val callerId = UUID.randomUUID()
        val calleeId = UUID.randomUUID()
        val created = startService.handle(newStartCallIn(callerId = callerId, calleeId = calleeId)).entity
        endService.handle(EndCallCommand.In(callId = created.id, endedByUserId = calleeId))

        val result = activeService.handle(GetActiveCallQuery.In(userId = callerId))

        assertEquals(null, result.call)
    }

    private fun newStartCallIn(callerId: UUID, calleeId: UUID) = StartCallCommand.In(
        callerId = callerId,
        calleeId = calleeId,
        counterpartUserId = calleeId,
    )
}
