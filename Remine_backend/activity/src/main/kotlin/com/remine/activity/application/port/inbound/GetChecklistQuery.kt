package com.remine.activity.application.port.inbound

import com.remine.activity.domain.ActivityChecklistItem
import java.time.LocalDate
import java.util.UUID

interface GetChecklistQuery {
    fun handle(query: In): Out

    data class In(
        val userId: UUID,
        val statDate: LocalDate,
    )

    data class Out(
        val items: List<ActivityChecklistItem>,
    )
}
