package com.remine.user.application.service

import com.remine.common.domain.exception.EntityNotFoundException
import com.remine.user.application.port.inbound.GetPairedUserQuery
import com.remine.user.application.port.outbound.UserRepositoryPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetPairedUserService(
    private val userRepositoryPort: UserRepositoryPort,
) : GetPairedUserQuery {

    override fun handle(query: GetPairedUserQuery.In): GetPairedUserQuery.Out {
        val user = userRepositoryPort.findById(query.userId)
            ?: throw EntityNotFoundException("User not found: ${query.userId}")

        val pairedUserId = user.pairedUserId ?: return GetPairedUserQuery.Out(pairedEntity = null)
        val pairedUser = userRepositoryPort.findById(pairedUserId)
        return GetPairedUserQuery.Out(pairedEntity = pairedUser)
    }
}
