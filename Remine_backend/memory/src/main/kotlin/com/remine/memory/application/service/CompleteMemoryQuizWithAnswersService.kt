package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.CompleteMemoryQuizWithAnswersCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizGeneratorPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.MemoryQuizQuestion
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompleteMemoryQuizWithAnswersService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizDraftQuestionRepository: MemoryQuizDraftQuestionRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
    private val memoryQuizGenerator: MemoryQuizGeneratorPort,
) : CompleteMemoryQuizWithAnswersCommand {

    override fun handle(command: CompleteMemoryQuizWithAnswersCommand.In): CompleteMemoryQuizWithAnswersCommand.Out {
        val photo = memoryPhotoRepository.findById(command.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${command.memoryPhotoId}")
        requireOwnedByCaller(photo, command.ownerUserId)

        if (command.answers.isEmpty()) {
            throw InvalidRequestException("답변 목록이 비어있습니다")
        }

        val draftQuestions = memoryQuizDraftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(command.memoryPhotoId)
        val draftMap = draftQuestions.associateBy { it.id }

        val qaList = command.answers.mapIndexed { index, item ->
            if (item.answer.isBlank()) {
                throw InvalidRequestException("문항 ${index + 1}의 정답 내용이 비어있습니다")
            }
            val questionText = when {
                !item.question.isNullOrBlank() -> item.question.trim()
                item.draftQuestionId != null && draftMap.containsKey(item.draftQuestionId) -> draftMap[item.draftQuestionId]!!.question
                index < draftQuestions.size -> draftQuestions[index].question
                else -> throw InvalidRequestException("문항 ${index + 1}에 해당하는 질문 정보를 찾을 수 없습니다")
            }
            MemoryQuizGeneratorPort.QuestionAndAnswer(
                question = questionText,
                answer = item.answer.trim(),
            )
        }

        val distractorsList = memoryQuizGenerator.generateDistractors(qaList)
        if (distractorsList.size != qaList.size) {
            throw InvalidRequestException("AI 보기 생성에 실패했어요: 생성된 문항 수가 일치하지 않습니다")
        }

        val finalQuestions = qaList.zip(distractorsList).mapIndexed { index, (qa, distractorItem) ->
            val validDistractors = distractorItem.distractors
                .filter { it.trim() != qa.answer.trim() }
                .distinct()

            if (validDistractors.size < 3) {
                throw InvalidRequestException("AI 보기 생성에 실패했어요: 문항 '${qa.question}'의 오답 보기가 부족합니다")
            }

            val allOptions = (listOf(qa.answer) + validDistractors.take(3)).shuffled()
            val correctOptionIndex = allOptions.indexOf(qa.answer)

            MemoryQuizQuestion(
                memoryPhotoId = command.memoryPhotoId,
                question = qa.question,
                options = allOptions,
                correctOptionIndex = correctOptionIndex,
                sortOrder = index,
            )
        }

        // Clean up previous final questions and draft questions to prevent duplicate accumulation
        memoryQuizQuestionRepository.deleteAllByMemoryPhotoId(command.memoryPhotoId)
        memoryQuizDraftQuestionRepository.deleteAllByMemoryPhotoId(command.memoryPhotoId)

        val savedQuestions = memoryQuizQuestionRepository.saveAll(finalQuestions)
        memoryPhotoRepository.save(photo.withStatus(MemoryPhotoStatus.QUIZ_ACTIVE))

        return CompleteMemoryQuizWithAnswersCommand.Out(questions = savedQuestions)
    }
}
