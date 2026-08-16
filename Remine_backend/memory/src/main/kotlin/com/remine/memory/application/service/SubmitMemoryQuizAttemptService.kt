package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.SubmitMemoryQuizAttemptCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryQuizAttempt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class SubmitMemoryQuizAttemptService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
    private val memoryQuizAttemptRepository: MemoryQuizAttemptRepositoryPort,
) : SubmitMemoryQuizAttemptCommand {

    override fun handle(command: SubmitMemoryQuizAttemptCommand.In): SubmitMemoryQuizAttemptCommand.Out {
        memoryPhotoRepository.findById(command.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${command.memoryPhotoId}")

        val questions = memoryQuizQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(command.memoryPhotoId)
        if (questions.isEmpty()) {
            throw InvalidRequestException("No quiz questions found for photo: ${command.memoryPhotoId}")
        }

        var correctCount = 0
        for ((index, question) in questions.withIndex()) {
            if (index < command.answers.size && command.answers[index] == question.correctOptionIndex) {
                correctCount++
            }
        }

        val attempt = MemoryQuizAttempt(
            memoryPhotoId = command.memoryPhotoId,
            respondentUserId = command.respondentUserId,
            correctCount = correctCount,
            totalCount = questions.size,
            completedAt = Instant.now(),
        )
        memoryQuizAttemptRepository.save(attempt)

        return SubmitMemoryQuizAttemptCommand.Out(
            correctCount = correctCount,
            totalCount = questions.size,
        )
    }
}
