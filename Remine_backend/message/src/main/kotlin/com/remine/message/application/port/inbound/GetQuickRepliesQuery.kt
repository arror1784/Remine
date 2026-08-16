package com.remine.message.application.port.inbound

import com.remine.message.domain.QuickReply

interface GetQuickRepliesQuery {
    fun handle(query: In): Out

    data class In(
        val role: String,
    )

    data class Out(
        val items: List<QuickReply>,
    )
}
