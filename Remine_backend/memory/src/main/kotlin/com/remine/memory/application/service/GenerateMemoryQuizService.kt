package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.GenerateMemoryQuizCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizQuestion
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GenerateMemoryQuizService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
    private val memoryQuizGenerator: MemoryQuizGeneratorPort,
) : GenerateMemoryQuizCommand {

    override fun handle(command: GenerateMemoryQuizCommand.In): GenerateMemoryQuizCommand.Out {
        val photo = memoryPhotoRepository.findById(command.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${command.memoryPhotoId}")
        requireOwnedByCaller(photo, command.ownerUserId)

        val generated = memoryQuizGenerator.generateQuestions(photo)
        if (generated.isEmpty()) {
            throw InvalidRequestException("AI 퀴즈 생성에 실패했어요: 생성된 문항이 없습니다")
        }

        val questionEntities = generated.mapIndexed { index, q ->
            MemoryQuizQuestion(
                memoryPhotoId = command.memoryPhotoId,
                question = q.question,
                options = q.options,
                correctOptionIndex = q.correctOptionIndex,
                sortOrder = index,
            )
        }

        val savedQuestions = memoryQuizQuestionRepository.saveAll(questionEntities)
        memoryPhotoRepository.save(photo.withStatus(MemoryPhotoStatus.QUIZ_ACTIVE))

        return GenerateMemoryQuizCommand.Out(questions = savedQuestions)
    }
}
