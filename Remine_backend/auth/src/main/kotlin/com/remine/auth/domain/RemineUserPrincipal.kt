package com.remine.auth.domain

import com.remine.common.domain.exception.InvalidRequestException
import java.util.UUID

/**
 * Resolved once per request from the JWT claims — never re-derived from the
 * DB. If pairing changes after this token was issued, the caller must
 * re-login to pick up the new claims (accepted MVP limitation).
 */
data class RemineUserPrincipal(
    val userId: UUID,
    val role: Role,
    val pairedUserId: UUID?,
) {
    /** The parent's userId regardless of whether the caller is the parent or the paired child. */
    fun parentUserId(): UUID = when (role) {
        Role.PARENT -> userId
        Role.CHILD -> pairedUserId ?: throw InvalidRequestException("Child account is not paired with a parent yet")
    }

    /** The "other side" of the pair — who a message/call from this principal should go to. */
    fun counterpartUserId(): UUID? = pairedUserId

    /** Same as [counterpartUserId] but for flows that cannot proceed unpaired. */
    fun requireCounterpartUserId(): UUID = when (role) {
        Role.PARENT -> pairedUserId ?: throw InvalidRequestException("Parent account is not paired with a child yet")
        Role.CHILD -> parentUserId()
    }
}
