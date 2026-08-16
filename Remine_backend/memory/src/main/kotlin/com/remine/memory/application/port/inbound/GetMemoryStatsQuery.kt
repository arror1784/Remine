package com.remine.memory.application.port.inbound

import java.util.UUID

interface GetMemoryStatsQuery {
    fun handle(query: In): Out

    data class In(
        val ownerUserId: UUID,
    )

    data class Out(
        val totalPhotos: Int,
        val quizActiveCount: Int,
        val addedThisMonth: Int,
    )
}
