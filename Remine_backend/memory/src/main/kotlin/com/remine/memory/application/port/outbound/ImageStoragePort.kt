package com.remine.memory.application.port.outbound

/**
 * Persists raw image bytes somewhere publicly fetchable and returns the URL to read them back
 * from. The local-disk adapter is the only implementation today; swapping to S3 or another
 * provider later only touches that adapter, not callers of this port.
 */
interface ImageStoragePort {
    fun store(bytes: ByteArray, originalFilename: String, contentType: String): String
}
