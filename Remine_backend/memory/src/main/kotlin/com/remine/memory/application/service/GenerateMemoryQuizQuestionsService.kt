package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.GenerateMemoryQuizQuestionsCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.domain.MemoryQuizDraftQuestion
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class GenerateMemoryQuizQuestionsService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizDraftQuestionRepository: MemoryQuizDraftQuestionRepositoryPort,
    private val memoryQuizGenerator: MemoryQuizGeneratorPort,
) : GenerateMemoryQuizQuestionsCommand {

    override fun handle(command: GenerateMemoryQuizQuestionsCommand.In): GenerateMemoryQuizQuestionsCommand.Out {
        val photo = memoryPhotoRepository.findById(command.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${command.memoryPhotoId}")
        requireOwnedByCaller(photo, command.ownerUserId)

        val count = if (command.count in 1..10) command.count else 3
        val questionTexts = memoryQuizGenerator.generateDraftQuestions(photo, count)
        if (questionTexts.isEmpty()) {
            throw InvalidRequestException("AI 질문 생성에 실패했어요: 생성된 문항이 없습니다")
        }

        // Cleanly replace any previous draft state to prevent duplicate accumulation
        memoryQuizDraftQuestionRepository.deleteAllByMemoryPhotoId(command.memoryPhotoId)

        val draftEntities = questionTexts.mapIndexed { index, question ->
            MemoryQuizDraftQuestion(
                memoryPhotoId = command.memoryPhotoId,
                question = question,
                sortOrder = index,
            )
        }

        val savedQuestions = memoryQuizDraftQuestionRepository.saveAll(draftEntities)
        return GenerateMemoryQuizQuestionsCommand.Out(questions = savedQuestions)
    }
}
