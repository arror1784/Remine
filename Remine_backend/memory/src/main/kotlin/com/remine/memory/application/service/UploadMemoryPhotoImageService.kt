package com.remine.memory.application.service

import com.remine.common.domain.exception.InvalidRequestException
import com.remine.memory.application.port.inbound.UploadMemoryPhotoImageCommand
import com.remine.memory.application.port.outbound.ImageStoragePort
import org.springframework.stereotype.Service

@Service
class UploadMemoryPhotoImageService(
    private val imageStoragePort: ImageStoragePort,
) : UploadMemoryPhotoImageCommand {

    override fun handle(command: UploadMemoryPhotoImageCommand.In): UploadMemoryPhotoImageCommand.Out {
        if (!command.contentType.startsWith("image/")) {
            throw InvalidRequestException("이미지 파일만 업로드할 수 있어요")
        }
        if (command.bytes.isEmpty()) {
            throw InvalidRequestException("빈 파일은 업로드할 수 없어요")
        }
        if (command.bytes.size > MAX_IMAGE_BYTES) {
            throw InvalidRequestException("이미지는 5MB 이하만 업로드할 수 있어요")
        }

        val url = imageStoragePort.store(command.bytes, command.originalFilename, command.contentType)
        return UploadMemoryPhotoImageCommand.Out(url = url)
    }

    private companion object {
        const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    }
}
