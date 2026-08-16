package com.remine.app.composition

import com.remine.activity.application.port.inbound.GetWeeklyPatternQuery
import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.user.application.port.inbound.GetMyProfileQuery
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Cross-domain aggregation only — see FamilySummaryController for why this
 * lives in app-api rather than any single domain module.
 */
@RestController
class MyPageStatsController(
    private val getMyProfileQuery: GetMyProfileQuery,
    private val getMemoryStatsQuery: GetMemoryStatsQuery,
    private val getWeeklyPatternQuery: GetWeeklyPatternQuery,
) {

    @GetMapping("/api/v1/users/me/stats")
    fun stats(@AuthenticationPrincipal principal: RemineUserPrincipal): ApiResponse<MyPageStatsResponse> {
        val parentUserId = principal.parentUserId()

        val profile = getMyProfileQuery.handle(GetMyProfileQuery.In(userId = parentUserId)).entity
        val memoryStats = getMemoryStatsQuery.handle(GetMemoryStatsQuery.In(ownerUserId = parentUserId))
        val weekly = getWeeklyPatternQuery.handle(GetWeeklyPatternQuery.In(userId = parentUserId))
        val activeDaysThisWeek = weekly.days.count { it.steps > 0 }

        return ApiResponse.ok(
            MyPageStatsResponse(
                streakDays = profile.streakDays,
                sharedPhotoCount = memoryStats.totalPhotos,
                quizActiveCount = memoryStats.quizActiveCount,
                activeDaysThisWeek = activeDaysThisWeek,
                totalDaysThisWeek = weekly.days.size,
            ),
        )
    }
}

data class MyPageStatsResponse(
    val streakDays: Int,
    val sharedPhotoCount: Int,
    val quizActiveCount: Int,
    val activeDaysThisWeek: Int,
    val totalDaysThisWeek: Int,
)
