package com.remine.message.application.port.inbound

import java.util.UUID

interface CountChatThreadQuery {
    fun handle(query: In): Out

    data class In(
        val userAId: UUID,
        val userBId: UUID,
    )

    data class Out(
        val count: Int,
    )
}
