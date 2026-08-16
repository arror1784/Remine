package com.remine.activity.adapter.presentation.web

import javax.validation.Valid
import javax.validation.constraints.NotNull

data class SyncDailyActivityRequest(
    @field:NotNull
    @field:Valid
    val entries: List<SyncDailyActivityEntryRequest> = emptyList(),
)
