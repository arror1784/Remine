package com.remine.memory.application.port.inbound

import java.util.UUID

interface SubmitMemoryQuizAttemptCommand {
    fun handle(command: In): Out

    data class In(
        val memoryPhotoId: UUID,
        val respondentUserId: UUID,
        val answers: List<Int>,
        /** The parent the caller is acting for — must match the photo's owner. */
        val ownerUserId: UUID,
    )

    data class Out(
        val correctCount: Int,
        val totalCount: Int,
    )
}
