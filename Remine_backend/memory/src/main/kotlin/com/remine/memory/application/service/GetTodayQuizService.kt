package com.remine.memory.application.service

import com.remine.memory.application.port.inbound.GetTodayQuizQuery
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizAttemptRepositoryPort
import com.remine.memory.application.port.outbound.MemoryQuizQuestionRepositoryPort
import com.remine.memory.domain.MemoryPhotoStatus
import com.remine.memory.domain.QuestionView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneOffset

@Service
@Transactional(readOnly = true)
class GetTodayQuizService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
    private val memoryQuizQuestionRepository: MemoryQuizQuestionRepositoryPort,
    private val memoryQuizAttemptRepository: MemoryQuizAttemptRepositoryPort,
) : GetTodayQuizQuery {

    override fun handle(query: GetTodayQuizQuery.In): GetTodayQuizQuery.Out {
        val startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant()
        val activePhotos = memoryPhotoRepository.findAllByOwnerUserIdAndStatusOrderByCreatedAtDesc(
            ownerUserId = query.ownerUserId,
            status = MemoryPhotoStatus.QUIZ_ACTIVE,
        )

        val qualifyingPhoto = activePhotos.firstOrNull { photo ->
            !memoryQuizAttemptRepository.existsByMemoryPhotoIdAndCompletedAtGreaterThanEqual(photo.id, startOfToday)
        }

        if (qualifyingPhoto == null) {
            return GetTodayQuizQuery.Out(
                photo = null,
                questions = emptyList(),
            )
        }

        val questions = memoryQuizQuestionRepository.findAllByMemoryPhotoIdOrderBySortOrderAsc(qualifyingPhoto.id)
        val questionViews = questions.map {
            QuestionView(
                id = it.id,
                question = it.question,
                options = it.options,
            )
        }

        return GetTodayQuizQuery.Out(
            photo = qualifyingPhoto,
            questions = questionViews,
        )
    }
}
