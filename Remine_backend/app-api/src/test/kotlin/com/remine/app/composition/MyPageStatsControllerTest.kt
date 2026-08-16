package com.remine.app.composition

import com.remine.activity.application.port.inbound.GetWeeklyPatternQuery
import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.user.application.port.inbound.GetMyProfileQuery
import com.remine.user.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class MyPageStatsControllerTest {

    @Test
    fun `aggregates profile streak, memory stats, and weekly active days for a parent`() {
        val parentId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = parentId, role = Role.PARENT, pairedUserId = null)

        val fakeProfile = object : GetMyProfileQuery {
            override fun handle(query: GetMyProfileQuery.In) = GetMyProfileQuery.Out(
                entity = User(id = parentId, role = Role.PARENT, name = "부모", ageGroup = "60대", streakDays = 7),
            )
        }
        val fakeMemoryStats = object : GetMemoryStatsQuery {
            override fun handle(query: GetMemoryStatsQuery.In) =
                GetMemoryStatsQuery.Out(totalPhotos = 10, quizActiveCount = 4, addedThisMonth = 1)
        }
        val fakeWeeklyPattern = object : GetWeeklyPatternQuery {
            override fun handle(query: GetWeeklyPatternQuery.In) = GetWeeklyPatternQuery.Out(
                days = listOf(
                    GetWeeklyPatternQuery.DayPoint(LocalDate.now().minusDays(2), steps = 500, isToday = false),
                    GetWeeklyPatternQuery.DayPoint(LocalDate.now().minusDays(1), steps = 0, isToday = false),
                    GetWeeklyPatternQuery.DayPoint(LocalDate.now(), steps = 300, isToday = true),
                ),
            )
        }

        val response = MyPageStatsController(fakeProfile, fakeMemoryStats, fakeWeeklyPattern).stats(principal)

        assertEquals(7, response.data?.streakDays)
        assertEquals(10, response.data?.sharedPhotoCount)
        assertEquals(4, response.data?.quizActiveCount)
        assertEquals(2, response.data?.activeDaysThisWeek)
        assertEquals(3, response.data?.totalDaysThisWeek)
    }

    @Test
    fun `a CHILD principal's stats resolve against their paired parent's data`() {
        val parentId = UUID.randomUUID()
        val childId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = childId, role = Role.CHILD, pairedUserId = parentId)
        var queriedUserId: UUID? = null

        val fakeProfile = object : GetMyProfileQuery {
            override fun handle(query: GetMyProfileQuery.In): GetMyProfileQuery.Out {
                queriedUserId = query.userId
                return GetMyProfileQuery.Out(
                    entity = User(id = parentId, role = Role.PARENT, name = "부모", ageGroup = "60대", streakDays = 1),
                )
            }
        }
        val fakeMemoryStats = object : GetMemoryStatsQuery {
            override fun handle(query: GetMemoryStatsQuery.In) =
                GetMemoryStatsQuery.Out(totalPhotos = 0, quizActiveCount = 0, addedThisMonth = 0)
        }
        val fakeWeeklyPattern = object : GetWeeklyPatternQuery {
            override fun handle(query: GetWeeklyPatternQuery.In) = GetWeeklyPatternQuery.Out(days = emptyList())
        }

        MyPageStatsController(fakeProfile, fakeMemoryStats, fakeWeeklyPattern).stats(principal)

        assertEquals(parentId, queriedUserId)
    }
}
