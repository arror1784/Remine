package com.remine.memory.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.memory.application.port.inbound.GetMemoryQuizQuery
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.QuestionView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMemoryQuizService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
) : GetMemoryQuizQuery {

    override fun handle(query: GetMemoryQuizQuery.In): GetMemoryQuizQuery.Out {
        val photo = memoryPhotoRepository.findById(query.memoryPhotoId)
            ?: throw EntityNotFoundException("Memory photo not found: ${query.memoryPhotoId}")
        requireOwnedByCaller(photo, query.ownerUserId)

        val questions = memoryQuizQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(query.memoryPhotoId)
        val questionViews = questions.map {
            QuestionView(
                id = it.id,
                question = it.question,
                options = it.options,
            )
        }

        return GetMemoryQuizQuery.Out(
            photo = photo,
            questions = questionViews,
        )
    }
}
