package com.remine.memory.application.service

import com.remine.memory.application.port.inbound.GetMemoryStatsQuery
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.domain.MemoryPhotoStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.time.ZoneOffset

@Service
@Transactional(readOnly = true)
class GetMemoryStatsService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
) : GetMemoryStatsQuery {

    override fun handle(query: GetMemoryStatsQuery.In): GetMemoryStatsQuery.Out {
        val totalPhotos = memoryPhotoRepository.countByOwnerUserId(query.ownerUserId)
        val quizActiveCount = memoryPhotoRepository.countByOwnerUserIdAndStatus(query.ownerUserId, MemoryPhotoStatus.QUIZ_ACTIVE)
        val startOfMonth = YearMonth.now(ZoneOffset.UTC).atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        val addedThisMonth = memoryPhotoRepository.countByOwnerUserIdAndCreatedAtGreaterThanEqual(query.ownerUserId, startOfMonth)

        return GetMemoryStatsQuery.Out(
            totalPhotos = totalPhotos,
            quizActiveCount = quizActiveCount,
            addedThisMonth = addedThisMonth,
        )
    }
}
