package com.remine.activity.adapter.presentation.web

import javax.validation.constraints.Min

data class UpdateDailyActivityRequest(
    @field:Min(0)
    val sleepMinutes: Int? = null,

    @field:Min(0)
    val steps: Int? = null,

    @field:Min(0)
    val outingCount: Int? = null,

    @field:Min(0)
    val socialContactCount: Int? = null,
)
