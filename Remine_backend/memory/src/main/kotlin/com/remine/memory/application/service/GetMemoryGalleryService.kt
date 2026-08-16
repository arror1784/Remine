package com.remine.memory.application.service

import com.remine.memory.application.port.inbound.GetMemoryGalleryQuery
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMemoryGalleryService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
) : GetMemoryGalleryQuery {

    override fun handle(query: GetMemoryGalleryQuery.In): GetMemoryGalleryQuery.Out {
        val items = memoryPhotoRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(query.ownerUserId)
        return GetMemoryGalleryQuery.Out(items = items)
    }
}
