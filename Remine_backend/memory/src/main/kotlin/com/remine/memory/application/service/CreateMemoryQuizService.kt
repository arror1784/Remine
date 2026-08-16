package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.CreateMemoryQuizCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizQuestion
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateMemoryQuizService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
) : CreateMemoryQuizCommand {

    override fun handle(command: CreateMemoryQuizCommand.In): CreateMemoryQuizCommand.Out {
        val photo = memoryPhotoRepository.findById(command.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${command.memoryPhotoId}")
        requireOwnedByCaller(photo, command.ownerUserId)

        if (command.questions.isEmpty()) {
            throw InvalidRequestException("Quiz questions list cannot be empty")
        }

        val questionEntities = command.questions.mapIndexed { index, q ->
            MemoryQuizQuestion(
                memoryPhotoId = command.memoryPhotoId,
                question = q.question,
                options = q.options,
                correctOptionIndex = q.correctOptionIndex,
                sortOrder = index,
            )
        }

        val savedQuestions = memoryQuizQuestionRepository.saveAll(questionEntities)
        val updatedPhoto = photo.withStatus(MemoryPhotoStatus.QUIZ_ACTIVE)
        memoryPhotoRepository.save(updatedPhoto)

        return CreateMemoryQuizCommand.Out(questions = savedQuestions)
    }
}
