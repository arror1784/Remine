package com.remine.user.adapter.presentation.web

import com.remine.auth.domain.Role
import com.remine.user.application.port.inbound.DemoLoginCommand
import com.remine.user.domain.DemoVariant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class AuthControllerTest {

    @Test
    fun `demo login passes the request's role and variant through to the command`() {
        var captured: DemoLoginCommand.In? = null
        val fakeCommand = object : DemoLoginCommand {
            override fun handle(command: DemoLoginCommand.In): DemoLoginCommand.Out {
                captured = command
                return DemoLoginCommand.Out(
                    userId = UUID.randomUUID(),
                    role = command.role,
                    name = "테스트",
                    accessToken = "token",
                    pairedUserId = null,
                )
            }
        }
        val controller = AuthController(fakeCommand)

        controller.demoLogin(DemoLoginRequest(role = Role.CHILD, variant = DemoVariant.DEMO))

        assertEquals(Role.CHILD, captured?.role)
        assertEquals(DemoVariant.DEMO, captured?.variant)
    }

    @Test
    fun `omitting variant defaults to EVAL`() {
        var captured: DemoLoginCommand.In? = null
        val fakeCommand = object : DemoLoginCommand {
            override fun handle(command: DemoLoginCommand.In): DemoLoginCommand.Out {
                captured = command
                return DemoLoginCommand.Out(
                    userId = UUID.randomUUID(),
                    role = command.role,
                    name = "테스트",
                    accessToken = "token",
                    pairedUserId = null,
                )
            }
        }
        val controller = AuthController(fakeCommand)

        controller.demoLogin(DemoLoginRequest(role = Role.PARENT))

        assertEquals(DemoVariant.EVAL, captured?.variant)
    }
}
