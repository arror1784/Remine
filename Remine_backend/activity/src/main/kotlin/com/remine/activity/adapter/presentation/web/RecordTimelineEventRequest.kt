package com.remine.activity.adapter.presentation.web

import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant
import java.time.LocalDate
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class RecordTimelineEventRequest(
    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val statDate: LocalDate,

    @field:NotNull
    val occurredAt: Instant,

    @field:NotBlank
    val label: String,

    val colorHint: String? = null,
)
