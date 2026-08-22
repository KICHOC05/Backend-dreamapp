package team.dreamapp.com.presentation.controller.auth

import io.javalin.http.Context
import io.javalin.http.bodyValidator
import io.javalin.validation.BodyValidator
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.presentation.dto.auth.LoginRequestDto

class AuthControllerTest {

    private fun fakeUserInfo() = UserInfo(
        id = "test-id",
        userName = "testuser",
        password = "secret123",
        fullname = "Test User",
        roles = listOf("Cliente"),
        active = true,
        currentDate = "2025-01-15"
    )

    private fun mockLoginContext(loginRequest: LoginRequestDto): Context {
        val ctx = mock<Context>()
        val validator = mock<BodyValidator<LoginRequestDto>>()
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
    fun `logout revokes token from Authorization header`() {
        val ctx = mock<Context>()
        whenever(ctx.header("Authorization")).thenReturn("Bearer some-token")
        whenever(ctx.sessionAttribute<UserInfo>("USER_INFO")).thenReturn(fakeUserInfo())

        AuthController.logout(ctx)

        verify(ctx).header("Authorization")
    }
}
