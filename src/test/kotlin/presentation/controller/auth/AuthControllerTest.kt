package team.dreamapp.com.presentation.controller.auth

import io.javalin.http.Context
import io.javalin.http.bodyValidator
import io.javalin.validation.BodyValidator
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import team.dreamapp.com.infrastructure.service.auth.AuthSessionCookie
import team.dreamapp.com.presentation.dto.auth.LoginRequestDto

class AuthControllerTest {

    private fun mockLoginContext(loginRequest: LoginRequestDto): Context {
        val ctx = mock<Context>()
        val validator = mock<BodyValidator<LoginRequestDto?>>()
        whenever(validator.get()).thenReturn(loginRequest)
        whenever(ctx.bodyValidator<LoginRequestDto>()).thenReturn(validator)
        whenever(ctx.status(400)).thenReturn(ctx)
        return ctx
    }

    @Test
    fun `login returns 400 when username is blank`() {
        val ctx = mockLoginContext(LoginRequestDto(userName = "", password = "pass1234567"))

        AuthController.login(ctx)

        verify(ctx).status(400)
    }

    @Test
    fun `login returns 400 when password is blank`() {
        val ctx = mockLoginContext(LoginRequestDto(userName = "testuser", password = ""))

        AuthController.login(ctx)

        verify(ctx).status(400)
    }

    @Test
    fun `logout revokes and clears the HttpOnly session cookie`() {
        val ctx = mock<Context>()
        whenever(ctx.cookie(AuthSessionCookie.name())).thenReturn("some-token")

        AuthController.logout(ctx)

        verify(ctx).cookie(AuthSessionCookie.name())
        verify(ctx).header("Set-Cookie", AuthSessionCookie.serialize("", 0))
    }
}
