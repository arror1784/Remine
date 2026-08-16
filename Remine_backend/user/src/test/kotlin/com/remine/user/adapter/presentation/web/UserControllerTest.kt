package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.RemineUserPrincipal
import com.remine.auth.domain.Role
import com.remine.user.application.port.inbound.GetMyProfileQuery
import com.remine.user.application.port.inbound.GetPairedUserQuery
import com.remine.user.application.port.inbound.PairWithInviteCodeCommand
import com.remine.user.application.port.inbound.SignUpCommand
import com.remine.user.application.port.inbound.UpdateProfileCommand
import com.remine.user.domain.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.access.prepost.PreAuthorize
import java.util.UUID

class UserControllerTest {

    private lateinit var userController: UserController

    private val sampleUserId = UUID.randomUUID()
    private val sampleParentId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        val fakeSignUp = object : SignUpCommand {
            override fun handle(command: SignUpCommand.In): SignUpCommand.Out {
                return SignUpCommand.Out(
                    userId = sampleUserId,
                    inviteCode = if (command.role == Role.PARENT) "REMIND-ABCD" else null,
                    accessToken = "sample-token",
                )
            }
        }
        val fakeUpdate = object : UpdateProfileCommand {
            override fun handle(command: UpdateProfileCommand.In): UpdateProfileCommand.Out {
                return UpdateProfileCommand.Out(
                    entity = User(
                        id = command.userId,
                        role = Role.CHILD,
                        name = command.name ?: "기존이름",
                        ageGroup = command.ageGroup ?: "20대",
                    ),
                )
            }
        }
        val fakePair = object : PairWithInviteCodeCommand {
            override fun handle(command: PairWithInviteCodeCommand.In): PairWithInviteCodeCommand.Out {
                return PairWithInviteCodeCommand.Out(
                    parentUserId = sampleParentId,
                    accessToken = "new-paired-token",
                )
            }
        }
        val fakeGetMyProfile = object : GetMyProfileQuery {
            override fun handle(query: GetMyProfileQuery.In): GetMyProfileQuery.Out {
                return GetMyProfileQuery.Out(
                    entity = User(
                        id = query.userId,
                        role = Role.CHILD,
                        name = "내이름",
                        ageGroup = "20대",
                    ),
                )
            }
        }
        val fakeGetPaired = object : GetPairedUserQuery {
            override fun handle(query: GetPairedUserQuery.In): GetPairedUserQuery.Out {
                return GetPairedUserQuery.Out(
                    pairedEntity = User(
                        id = sampleParentId,
                        role = Role.PARENT,
                        name = "부모님",
                        ageGroup = "60대",
                    ),
                )
            }
        }

        userController = UserController(
            signUpCommand = fakeSignUp,
            updateProfileCommand = fakeUpdate,
            pairWithInviteCodeCommand = fakePair,
            getMyProfileQuery = fakeGetMyProfile,
            getPairedUserQuery = fakeGetPaired,
        )
    }

    @Test
    fun `signUp endpoint returns ok response`() {
        val response = userController.signUp(
            SignUpRequest(
                role = Role.PARENT,
                name = "부모",
                ageGroup = "60대",
                interests = listOf("독서"),
            ),
        )
        assertEquals(sampleUserId, response.data?.userId)
        assertEquals("REMIND-ABCD", response.data?.inviteCode)
        assertEquals("sample-token", response.data?.accessToken)
    }

    @Test
    fun `pairWithInviteCode is restricted to CHILD role`() {
        val preAuthorize = UserController::class.java
            .methods
            .single { it.name == "pairWithInviteCode" }
            .getAnnotation(PreAuthorize::class.java)

        assertNotNull(preAuthorize)
        assertEquals("hasRole('CHILD')", preAuthorize.value)
    }

    @Test
    fun `pairWithInviteCode succeeds when called by CHILD`() {
        val childPrincipal = RemineUserPrincipal(
            userId = sampleUserId,
            role = Role.CHILD,
            pairedUserId = null,
        )

        val response = userController.pairWithInviteCode(
            principal = childPrincipal,
            request = PairingRequest(inviteCode = "REMIND-1234"),
        )

        assertEquals(sampleParentId, response.data?.parentUserId)
        assertEquals("new-paired-token", response.data?.accessToken)
    }
}
