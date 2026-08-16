package com.remine.user.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.user.application.port.inbound.GetMyProfileQuery
import com.remine.user.application.port.outbound.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMyProfileService(
    private val userRepositoryPort: UserRepositoryPort,
) : GetMyProfileQuery {

    override fun handle(query: GetMyProfileQuery.In): GetMyProfileQuery.Out {
        val user = userRepositoryPort.findById(query.userId)
            ?: throw EntityNotFoundException("User not found: ${query.userId}")
        return GetMyProfileQuery.Out(entity = user)
    }
}
