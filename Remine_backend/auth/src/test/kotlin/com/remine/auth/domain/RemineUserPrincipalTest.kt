package com.remine.auth.domain

import com.remine.common.domain.exception.InvalidRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class RemineUserPrincipalTest {

    @Test
    fun `parentUserId for a PARENT principal is its own userId`() {
        val userId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = userId, role = Role.PARENT, pairedUserId = null)

        assertEquals(userId, principal.parentUserId())
    }

    @Test
    fun `parentUserId for a paired CHILD principal is the pairedUserId`() {
        val parentId = UUID.randomUUID()
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.CHILD, pairedUserId = parentId)

        assertEquals(parentId, principal.parentUserId())
    }

    @Test
    fun `parentUserId for an unpaired CHILD principal throws InvalidRequestException`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.CHILD, pairedUserId = null)

        assertThrows<InvalidRequestException> { principal.parentUserId() }
    }

    @Test
    fun `counterpartUserId returns pairedUserId regardless of role`() {
        val pairedId = UUID.randomUUID()
        val parentPrincipal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.PARENT, pairedUserId = pairedId)
        val childPrincipal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.CHILD, pairedUserId = pairedId)

        assertEquals(pairedId, parentPrincipal.counterpartUserId())
        assertEquals(pairedId, childPrincipal.counterpartUserId())
    }

    @Test
    fun `counterpartUserId is null when unpaired`() {
        val principal = RemineUserPrincipal(userId = UUID.randomUUID(), role = Role.PARENT, pairedUserId = null)

        assertNull(principal.counterpartUserId())
    }
}
