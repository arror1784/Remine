package com.remine.memory.application.service

import com.remine.memory.application.port.inbound.UploadMemoryPhotoCommand
import com.remine.memory.application.port.outbound.MemoryPhotoRepositoryPort
import com.remine.memory.domain.MemoryPhoto
import com.remine.memory.domain.MemoryPhotoStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UploadMemoryPhotoService(
    private val memoryPhotoRepository: MemoryPhotoRepositoryPort,
) : UploadMemoryPhotoCommand {

    override fun handle(command: UploadMemoryPhotoCommand.In): UploadMemoryPhotoCommand.Out {
        val photo = MemoryPhoto(
            uploadedByUserId = command.uploadedByUserId,
            ownerUserId = command.ownerUserId,
            title = command.title,
            photoUrl = command.photoUrl,
            memoryLabel = command.memoryLabel,
            status = MemoryPhotoStatus.PENDING,
        )
        val saved = memoryPhotoRepository.save(photo)
        return UploadMemoryPhotoCommand.Out(entity = saved)
    }
}
