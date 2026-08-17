package com.remine.call.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.call.application.port.inbound.AnswerCallCommand
import com.remine.call.application.port.inbound.EndCallCommand
import com.remine.call.application.port.inbound.GetActiveCallQuery
import com.remine.call.application.port.inbound.GetCallHistoryQuery
import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.call.application.port.inbound.StartCallCommand
import com.remine.common.web.ApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/calls")
class CallController(
    private val startCallCommand: StartCallCommand,
    private val answerCallCommand: AnswerCallCommand,
    private val endCallCommand: EndCallCommand,
    private val getCallHistoryQuery: GetCallHistoryQuery,
    private val getCallStatsQuery: GetCallStatsQuery,
    private val getActiveCallQuery: GetActiveCallQuery,
) {
    @PostMapping
    fun startCall(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestBody(required = false) request: StartCallRequest? = null,
    ): ApiResponse<CallResponse> {
        val counterpartId = principal.requireCounterpartUserId()
        val out = startCallCommand.handle(
            StartCallCommand.In(
                callerId = principal.userId,
                calleeId = request?.calleeId ?: counterpartId,
                counterpartUserId = counterpartId,
            )
        )
        return ApiResponse.ok(CallResponse.from(out.entity))
    }

    @PatchMapping("/{id}/answer")
    fun answerCall(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<CallResponse> {
        val out = answerCallCommand.handle(
            AnswerCallCommand.In(
                callId = id,
                answeredByUserId = principal.userId,
            )
        )
        return ApiResponse.ok(CallResponse.from(out.entity))
    }

    @GetMapping("/active")
    fun getActiveCall(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<CallResponse?> {
        val out = getActiveCallQuery.handle(GetActiveCallQuery.In(userId = principal.userId))
        return ApiResponse.ok(out.call?.let { CallResponse.from(it) })
    }

    @PatchMapping("/{id}/end")
    fun endCall(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<CallResponse> {
        val out = endCallCommand.handle(
            EndCallCommand.In(
                callId = id,
                endedByUserId = principal.userId,
            )
        )
        return ApiResponse.ok(CallResponse.from(out.entity))
    }

    @GetMapping
    fun getCallHistory(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestParam(defaultValue = "20") limit: Int,
    ): ApiResponse<List<CallResponse>> {
        val out = getCallHistoryQuery.handle(
            GetCallHistoryQuery.In(
                userId = principal.userId,
                limit = limit.coerceIn(1, MAX_PAGE_SIZE),
            )
        )
        return ApiResponse.ok(out.items.map { CallResponse.from(it) })
    }

    @GetMapping("/stats")
    fun getCallStats(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<CallStatsResponse> {
        val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
        val out = getCallStatsQuery.handle(
            GetCallStatsQuery.In(
                userId = principal.userId,
                sinceMonthStart = firstDayOfMonth,
            )
        )
        return ApiResponse.ok(
            CallStatsResponse(
                count = out.count,
                totalDurationSeconds = out.totalDurationSeconds,
            )
        )
    }

    private companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
