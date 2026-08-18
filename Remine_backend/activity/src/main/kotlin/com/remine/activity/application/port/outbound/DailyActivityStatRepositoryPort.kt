package com.remine.activity.application.port.outbound

import com.remine.activity.domain.DailyActivityStat
import java.time.LocalDate
import java.util.UUID

interface DailyActivityStatRepositoryPort {
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): DailyActivityStat?
    fun findByUserIdAndStatDateIn(userId: UUID, statDates: Collection<LocalDate>): List<DailyActivityStat>
    fun findByUserIdAndStatDateBetween(userId: UUID, startDate: LocalDate, endDate: LocalDate): List<DailyActivityStat>
    fun save(stat: DailyActivityStat): DailyActivityStat
    fun saveAll(stats: Collection<DailyActivityStat>): List<DailyActivityStat>

    /** Used by the demo-reset utility (see app-api's DemoResetService) to wipe a demo account's history. */
    fun deleteAllByUserId(userId: UUID)
}
