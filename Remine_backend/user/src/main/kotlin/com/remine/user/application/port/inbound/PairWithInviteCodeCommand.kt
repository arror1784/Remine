package com.remine.user.application.port.inbound

import java.util.UUID

interface PairWithInviteCodeCommand {
    fun handle(command: In): Out

    data class In(
        val childUserId: UUID,
        val inviteCode: String,
    )

    data class Out(
        val parentUserId: UUID,
        val accessToken: String,
    )
}
