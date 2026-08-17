package com.remine.app.composition

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.call.application.port.inbound.GetCallStatsQuery
import com.remine.common.web.ApiResponse
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.message.application.port.inbound.CountChatThreadQuery
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * Cross-domain aggregation only — lives in app-api because no domain module
 * is allowed to depend on another (see the plan's "one architectural
 * decision that unlocks parallelism"). Each number here comes from a query
 * port that module already exposes.
 */
@RestController
class FamilySummaryController(
    private val getMemoryStatsQuery: GetMemoryStatsQuery,
    private val getCallStatsQuery: GetCallStatsQuery,
    private val countChatThreadQuery: CountChatThreadQuery,
) {

    @GetMapping("/api/v1/family/summary")
    fun summary(@AuthenticationPrincipal principal: RemineUserPrincipal): ApiResponse<FamilySummaryResponse> {
        val parentUserId = principal.parentUserId()
        val counterpartId = principal.counterpartUserId()

        val memoryStats = getMemoryStatsQuery.handle(GetMemoryStatsQuery.In(ownerUserId = parentUserId))
        val callStats = getCallStatsQuery.handle(
            GetCallStatsQuery.In(userId = principal.userId, sinceMonthStart = LocalDate.now().withDayOfMonth(1)),
        )
        val messageCount = counterpartId?.let {
            countChatThreadQuery.handle(CountChatThreadQuery.In(userAId = principal.userId, userBId = it)).count
        } ?: 0

        return ApiResponse.ok(
            FamilySummaryResponse(
                sharedPhotoCount = memoryStats.totalPhotos,
                quizTogetherCount = memoryStats.quizActiveCount,
                messageCount = messageCount,
                callCount = callStats.count,
            ),
        )
    }
}

data class FamilySummaryResponse(
    val sharedPhotoCount: Int,
    val quizTogetherCount: Int,
    val messageCount: Int,
    val callCount: Int,
)
