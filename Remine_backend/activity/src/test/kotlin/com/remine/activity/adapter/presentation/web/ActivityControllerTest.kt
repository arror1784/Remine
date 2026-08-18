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
import com.remine.activity.domain.DailyActivityRecommendation
import com.remine.activity.domain.DailyActivityRecommendationActionType
import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class ActivityControllerTest {

    private val parentId = UUID.randomUUID()
    private val childId = UUID.randomUUID()
    private val childPrincipal = RemineUserPrincipal(
        userId = childId,
        role = Role.CHILD,
        pairedUserId = parentId,
    )
    private val parentPrincipal = RemineUserPrincipal(
        userId = parentId,
        role = Role.PARENT,
        pairedUserId = childId,
    )

    @Test
    fun `getDailyRecommendation uses principal parentUserId for both parent and child`() {
        var queriedUserId: UUID? = null
        val recommendationQuery = object : GetDailyActivityRecommendationQuery {
            override fun handle(query: GetDailyActivityRecommendationQuery.In): GetDailyActivityRecommendationQuery.Out {
                queriedUserId = query.userId
                return GetDailyActivityRecommendationQuery.Out(
                    recommendation = DailyActivityRecommendation(
                        userId = query.userId,
                        statDate = LocalDate.now(),
                        parentMessage = "오늘 오후 산책 어떠세요?",
                        childMessage = "어머니가 오늘 아직 외출을 못 하셨어요.",
                        actionType = DailyActivityRecommendationActionType.WALK,
                    )
                )
            }
        }

        val controller = createController(recQuery = recommendationQuery)

        // When called by CHILD principal -> queries parentId
        val childRes = controller.getDailyRecommendation(principal = childPrincipal)
        assertEquals(parentId, queriedUserId)
        assertEquals("오늘 오후 산책 어떠세요?", childRes.data?.parentMessage)
        assertEquals("어머니가 오늘 아직 외출을 못 하셨어요.", childRes.data?.childMessage)
        assertEquals(DailyActivityRecommendationActionType.WALK, childRes.data?.actionType)
        assertNull(childRes.error)

        // When called by PARENT principal -> queries parentId
        val parentRes = controller.getDailyRecommendation(principal = parentPrincipal)
        assertEquals(parentId, queriedUserId)
        assertEquals("오늘 오후 산책 어떠세요?", parentRes.data?.parentMessage)
        assertEquals("어머니가 오늘 아직 외출을 못 하셨어요.", parentRes.data?.childMessage)
        assertEquals(DailyActivityRecommendationActionType.WALK, parentRes.data?.actionType)
        assertNull(parentRes.error)
    }

    @Test
    fun `getCheerMessageSuggestions passes parentUserId and the checklist item id`() {
        var capturedIn: GetCheerMessageSuggestionsQuery.In? = null
        val checklistItemId = UUID.randomUUID()
        val suggestionsQuery = object : GetCheerMessageSuggestionsQuery {
            override fun handle(query: GetCheerMessageSuggestionsQuery.In): GetCheerMessageSuggestionsQuery.Out {
                capturedIn = query
                return GetCheerMessageSuggestionsQuery.Out(suggestions = listOf("메시지1", "메시지2", "메시지3"))
            }
        }

        val controller = createController(cheerSuggestionsQuery = suggestionsQuery)
        val response = controller.getCheerMessageSuggestions(principal = childPrincipal, id = checklistItemId)

        assertEquals(checklistItemId, capturedIn?.checklistItemId)
        assertEquals(parentId, capturedIn?.requestedByParentUserId)
        assertEquals(listOf("메시지1", "메시지2", "메시지3"), response.data)
        assertNull(response.error)
    }

    private fun createController(
        recordCommand: RecordDailyActivityCommand = mockRecordCommand(),
        updateCommand: UpdateDailyActivityCommand = mockUpdateCommand(),
        syncCommand: SyncDailyActivityCommand = mockSyncCommand(),
        toggleCommand: ToggleChecklistItemCommand = mockToggleCommand(),
        cheerCommand: SendCheerCommand = mockCheerCommand(),
        timelineCommand: RecordTimelineEventCommand = mockTimelineCommand(),
        todayQuery: GetTodaySummaryQuery = mockTodayQuery(),
        weeklyQuery: GetWeeklyPatternQuery = mockWeeklyQuery(),
        checklistQuery: GetChecklistQuery = mockChecklistQuery(),
        timelineQuery: GetTimelineQuery = mockTimelineQuery(),
        recQuery: GetDailyActivityRecommendationQuery = mockRecQuery(),
        cheerSuggestionsQuery: GetCheerMessageSuggestionsQuery = mockCheerSuggestionsQuery(),
    ) = ActivityController(
        recordDailyActivityCommand = recordCommand,
        updateDailyActivityCommand = updateCommand,
        syncDailyActivityCommand = syncCommand,
        toggleChecklistItemCommand = toggleCommand,
        sendCheerCommand = cheerCommand,
        recordTimelineEventCommand = timelineCommand,
        getTodaySummaryQuery = todayQuery,
        getWeeklyPatternQuery = weeklyQuery,
        getChecklistQuery = checklistQuery,
        getTimelineQuery = timelineQuery,
        getDailyActivityRecommendationQuery = recQuery,
        getCheerMessageSuggestionsQuery = cheerSuggestionsQuery,
    )

    private fun mockRecordCommand() = object : RecordDailyActivityCommand {
        override fun handle(command: RecordDailyActivityCommand.In) = throw NotImplementedError()
    }
    private fun mockUpdateCommand() = object : UpdateDailyActivityCommand {
        override fun handle(command: UpdateDailyActivityCommand.In) = throw NotImplementedError()
    }
    private fun mockSyncCommand() = object : SyncDailyActivityCommand {
        override fun handle(command: SyncDailyActivityCommand.In) = throw NotImplementedError()
    }
    private fun mockToggleCommand() = object : ToggleChecklistItemCommand {
        override fun handle(command: ToggleChecklistItemCommand.In) = throw NotImplementedError()
    }
    private fun mockCheerCommand() = object : SendCheerCommand {
        override fun handle(command: SendCheerCommand.In) = throw NotImplementedError()
    }
    private fun mockTimelineCommand() = object : RecordTimelineEventCommand {
        override fun handle(command: RecordTimelineEventCommand.In) = throw NotImplementedError()
    }
    private fun mockTodayQuery() = object : GetTodaySummaryQuery {
        override fun handle(query: GetTodaySummaryQuery.In) = throw NotImplementedError()
    }
    private fun mockWeeklyQuery() = object : GetWeeklyPatternQuery {
        override fun handle(query: GetWeeklyPatternQuery.In) = throw NotImplementedError()
    }
    private fun mockChecklistQuery() = object : GetChecklistQuery {
        override fun handle(query: GetChecklistQuery.In) = throw NotImplementedError()
    }
    private fun mockTimelineQuery() = object : GetTimelineQuery {
        override fun handle(query: GetTimelineQuery.In) = throw NotImplementedError()
    }
    private fun mockRecQuery() = object : GetDailyActivityRecommendationQuery {
        override fun handle(query: GetDailyActivityRecommendationQuery.In) = throw NotImplementedError()
    }
    private fun mockCheerSuggestionsQuery() = object : GetCheerMessageSuggestionsQuery {
        override fun handle(query: GetCheerMessageSuggestionsQuery.In) = throw NotImplementedError()
    }
}
