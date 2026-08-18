package com.remine.memory.application.port.inbound

interface UploadMemoryPhotoImageCommand {
    fun handle(command: In): Out

    data class In(
        val bytes: ByteArray,
        val originalFilename: String,
        val contentType: String,
    )

    data class Out(
        val url: String,
    )
}
