package com.remine.activity.application.port.outbound

import com.remine.activity.domain.ActivityChecklistItem
import java.time.LocalDate
import java.util.UUID

interface ActivityChecklistItemRepositoryPort {
    fun findById(id: UUID): ActivityChecklistItem?
    fun findByUserIdAndStatDate(userId: UUID, statDate: LocalDate): List<ActivityChecklistItem>
    fun save(item: ActivityChecklistItem): ActivityChecklistItem
    fun saveAll(items: Collection<ActivityChecklistItem>): List<ActivityChecklistItem>

    /** Used by the demo-reset utility (see app-api's DemoResetService) to wipe a demo account's history. */
    fun deleteAllByUserId(userId: UUID)
}
