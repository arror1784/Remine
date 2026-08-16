package com.remine.activity.adapter.presentation.web

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class SyncDailyActivityEntryRequest(
    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val statDate: LocalDate,

    @field:Min(0)
    val sleepMinutes: Int = 0,

    @field:Min(0)
    val steps: Int = 0,

    @field:Min(0)
    val outingCount: Int = 0,

    @field:Min(0)
    val socialContactCount: Int = 0,
)
