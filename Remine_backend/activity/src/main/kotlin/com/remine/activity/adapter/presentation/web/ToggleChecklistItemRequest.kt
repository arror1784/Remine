package com.remine.activity.adapter.presentation.web

import javax.validation.constraints.NotNull

data class ToggleChecklistItemRequest(
    @field:NotNull
    val done: Boolean,
)
