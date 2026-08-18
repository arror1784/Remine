package com.remine.memory.adapter.infrastructure.storage

import com.remine.memory.application.port.outbound.ImageStoragePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

/**
 * Writes uploaded image bytes under `<upload-dir>/memory-photos/` on local disk. `app-api`
 * exposes that same directory as a static resource under `/uploads`, so the returned URL is
 * fetchable directly from the browser. Local-only for now — swap this adapter for an
 * `ImageStoragePort` implementation backed by real object storage when one is available.
 */
@Component
class LocalDiskImageStorageAdapter(
    @Value("\${storage.upload-dir:./uploads}") private val uploadDir: String,
    @Value("\${storage.public-base-url:http://localhost:8080}") private val publicBaseUrl: String,
) : ImageStoragePort {

    override fun store(bytes: ByteArray, originalFilename: String, contentType: String): String {
        val extension = originalFilename.substringAfterLast('.', missingDelimiterValue = "")
        val filename = UUID.randomUUID().toString() + if (extension.isNotBlank()) ".$extension" else ""

        val directory = Paths.get(uploadDir, SUBDIRECTORY)
        Files.createDirectories(directory)
        Files.write(directory.resolve(filename), bytes)

        return "$publicBaseUrl/uploads/$SUBDIRECTORY/$filename"
    }

    private companion object {
        const val SUBDIRECTORY = "memory-photos"
    }
}
