package com.remine.memory.application.port.inbound

import java.util.UUID

interface SubmitMemoryQuizAttemptCommand {
    fun handle(command: In): Out

    data class In(
        val memoryPhotoId: UUID,
        val respondentUserId: UUID,
        val answers: List<Int>,
    )

    data class Out(
        val correctCount: Int,
        val totalCount: Int,
    )
}
