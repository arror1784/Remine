package com.remine.memory.application.service

import com.remine.common.domain.exception.ForbiddenException
import com.remine.memory.domain.MemoryPhoto
import java.util.UUID

internal fun requireOwnedByCaller(photo: MemoryPhoto, ownerUserId: UUID) {
    if (photo.ownerUserId != ownerUserId) {
        throw ForbiddenException("Memory photo ${photo.id} does not belong to your family")
    }
}
