package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.memory.application.port.inbound.GetMemoryQuizDraftQuestionsQuery
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizDraftQuestionRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMemoryQuizDraftQuestionsService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizDraftQuestionRepository: MemoryQuizDraftQuestionRepositoryPort,
) : GetMemoryQuizDraftQuestionsQuery {

    override fun handle(query: GetMemoryQuizDraftQuestionsQuery.In): GetMemoryQuizDraftQuestionsQuery.Out {
        val photo = memoryPhotoRepository.findById(query.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${query.memoryPhotoId}")
        requireOwnedByCaller(photo, query.ownerUserId)

        val questions = memoryQuizDraftQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(query.memoryPhotoId)
        return GetMemoryQuizDraftQuestionsQuery.Out(questions = questions)
    }
}
