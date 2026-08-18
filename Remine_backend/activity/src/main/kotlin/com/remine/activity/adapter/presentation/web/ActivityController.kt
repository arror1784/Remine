package com.remine.activity.adapter.presentation.web

import com.remine.activity.application.port.inbound.GetCheerMessageSuggestionsQuery
import com.remine.activity.application.port.inbound.GetChecklistQuery
import com.remine.activity.application.port.inbound.GetDailyActivityRecommendationQuery
import com.remine.activity.application.port.inbound.GetTimelineQuery
import com.remine.activity.application.port.inbound.GetTodaySummaryQuery
import com.remine.activity.application.port.inbound.GetWeeklyPatternQuery
import com.remine.activity.application.port.inbound.RecordDailyActivityCommand
import com.remine.activity.application.port.inbound.RecordTimelineEventCommand
import com.remine.activity.application.port.inbound.SendCheerCommand
import com.remine.activity.application.port.inbound.SyncDailyActivityCommand
import com.remine.activity.application.port.inbound.ToggleChecklistItemCommand
import com.remine.activity.application.port.inbound.UpdateDailyActivityCommand
import com.remine.auth.domain.RemineUserPrincipal
import com.remine.common.web.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
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
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/activities")
class ActivityController(
    private val recordDailyActivityCommand: RecordDailyActivityCommand,
    private val updateDailyActivityCommand: UpdateDailyActivityCommand,
    private val syncDailyActivityCommand: SyncDailyActivityCommand,
    private val toggleChecklistItemCommand: ToggleChecklistItemCommand,
    private val sendCheerCommand: SendCheerCommand,
    private val recordTimelineEventCommand: RecordTimelineEventCommand,
    private val getTodaySummaryQuery: GetTodaySummaryQuery,
    private val getWeeklyPatternQuery: GetWeeklyPatternQuery,
    private val getChecklistQuery: GetChecklistQuery,
    private val getTimelineQuery: GetTimelineQuery,
    private val getDailyActivityRecommendationQuery: GetDailyActivityRecommendationQuery,
    private val getCheerMessageSuggestionsQuery: GetCheerMessageSuggestionsQuery,
) {

    @PostMapping
    fun recordDailyActivity(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: RecordDailyActivityRequest,
    ): ApiResponse<DailyActivityStatResponse> {
        val out = recordDailyActivityCommand.handle(
            RecordDailyActivityCommand.In(
                userId = principal.parentUserId(),
                statDate = request.statDate,
                sleepMinutes = request.sleepMinutes,
                steps = request.steps,
                outingCount = request.outingCount,
                socialContactCount = request.socialContactCount,
            )
        )
        return ApiResponse.ok(DailyActivityStatResponse.from(out.entity))
    }

    @PatchMapping("/{statDate}")
    fun updateDailyActivity(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) statDate: LocalDate,
        @Valid @RequestBody request: UpdateDailyActivityRequest,
    ): ApiResponse<DailyActivityStatResponse> {
        val out = updateDailyActivityCommand.handle(
            UpdateDailyActivityCommand.In(
                userId = principal.parentUserId(),
                statDate = statDate,
                sleepMinutes = request.sleepMinutes,
                steps = request.steps,
                outingCount = request.outingCount,
                socialContactCount = request.socialContactCount,
            )
        )
        return ApiResponse.ok(DailyActivityStatResponse.from(out.entity))
    }

    @PatchMapping("/sync")
    fun syncDailyActivity(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: SyncDailyActivityRequest,
    ): ApiResponse<SyncDailyActivityResponse> {
        val out = syncDailyActivityCommand.handle(
            SyncDailyActivityCommand.In(
                userId = principal.parentUserId(),
                entries = request.entries.map {
                    SyncDailyActivityCommand.In.Entry(
                        statDate = it.statDate,
                        sleepMinutes = it.sleepMinutes,
                        steps = it.steps,
                        outingCount = it.outingCount,
                        socialContactCount = it.socialContactCount,
                    )
                }
            )
        )
        return ApiResponse.ok(SyncDailyActivityResponse(savedCount = out.savedCount))
    }

    @GetMapping("/today")
    fun getTodaySummary(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<TodaySummaryResponse> {
        val out = getTodaySummaryQuery.handle(
            GetTodaySummaryQuery.In(userId = principal.parentUserId())
        )
        return ApiResponse.ok(
            TodaySummaryResponse(
                stat = out.stat?.let { DailyActivityStatResponse.from(it) },
                sleepPercent = out.sleepPercent,
                stepsPercent = out.stepsPercent,
                outingPercent = out.outingPercent,
                socialPercent = out.socialPercent,
            )
        )
    }

    @GetMapping("/recommendation")
    fun getDailyRecommendation(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<DailyActivityRecommendationResponse> {
        val out = getDailyActivityRecommendationQuery.handle(
            GetDailyActivityRecommendationQuery.In(userId = principal.parentUserId())
        )
        return ApiResponse.ok(DailyActivityRecommendationResponse.from(out.recommendation))
    }

    @GetMapping("/weekly")
    fun getWeeklyPattern(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
    ): ApiResponse<WeeklyPatternResponse> {
        val out = getWeeklyPatternQuery.handle(
            GetWeeklyPatternQuery.In(userId = principal.parentUserId())
        )
        return ApiResponse.ok(
            WeeklyPatternResponse(
                days = out.days.map {
                    WeeklyPatternDayPointResponse(
                        statDate = it.statDate,
                        steps = it.steps,
                        isToday = it.isToday,
                    )
                }
            )
        )
    }

    @GetMapping("/checklist")
    fun getChecklist(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
    ): ApiResponse<List<ActivityChecklistItemResponse>> {
        val statDate = date ?: LocalDate.now()
        val out = getChecklistQuery.handle(
            GetChecklistQuery.In(
                userId = principal.parentUserId(),
                statDate = statDate,
            )
        )
        return ApiResponse.ok(out.items.map { ActivityChecklistItemResponse.from(it) })
    }

    @PatchMapping("/checklist/{id}")
    fun toggleChecklistItem(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
        @Valid @RequestBody request: ToggleChecklistItemRequest,
    ): ApiResponse<ActivityChecklistItemResponse> {
        val out = toggleChecklistItemCommand.handle(
            ToggleChecklistItemCommand.In(
                checklistItemId = id,
                done = request.done,
                requestedByParentUserId = principal.parentUserId(),
            )
        )
        return ApiResponse.ok(ActivityChecklistItemResponse.from(out.entity))
    }

    @PostMapping("/checklist/{id}/cheers")
    fun sendCheer(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
    ): ApiResponse<ActivityCheerResponse?> {
        val out = sendCheerCommand.handle(
            SendCheerCommand.In(
                checklistItemId = id,
                senderUserId = principal.userId,
                requestedByParentUserId = principal.parentUserId(),
            )
        )
        return ApiResponse.ok(out.entity?.let { ActivityCheerResponse.from(it) })
    }

    /**
     * AI-generated cheer-message suggestions tailored to this checklist item's type and the
     * parent's actual activity data for that date — used to populate the quick-reply picker in
     * the cheer-message sheet instead of static canned text.
     */
    @GetMapping("/checklist/{id}/cheer-message-suggestions")
    fun getCheerMessageSuggestions(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @PathVariable id: UUID,
    ): ApiResponse<List<String>> {
        val out = getCheerMessageSuggestionsQuery.handle(
            GetCheerMessageSuggestionsQuery.In(
                checklistItemId = id,
                requestedByParentUserId = principal.parentUserId(),
            )
        )
        return ApiResponse.ok(out.suggestions)
    }

    @GetMapping("/timeline")
    fun getTimeline(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
    ): ApiResponse<List<ActivityTimelineEventResponse>> {
        val statDate = date ?: LocalDate.now()
        val out = getTimelineQuery.handle(
            GetTimelineQuery.In(
                userId = principal.parentUserId(),
                statDate = statDate,
            )
        )
        return ApiResponse.ok(out.events.map { ActivityTimelineEventResponse.from(it) })
    }

    @PostMapping("/timeline")
    fun recordTimelineEvent(
        @AuthenticationPrincipal principal: RemineUserPrincipal,
        @Valid @RequestBody request: RecordTimelineEventRequest,
    ): ApiResponse<ActivityTimelineEventResponse> {
        val out = recordTimelineEventCommand.handle(
            RecordTimelineEventCommand.In(
                userId = principal.parentUserId(),
                statDate = request.statDate,
                occurredAt = request.occurredAt,
                label = request.label,
                colorHint = request.colorHint,
            )
        )
        return ApiResponse.ok(ActivityTimelineEventResponse.from(out.entity))
    }
}
