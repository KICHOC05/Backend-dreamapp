package team.dreamapp.com.domain.usecase.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import io.javalin.http.Context
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.domain.services.auth.AuthService

class LoginUseCaseTest {

    private val authService = mock<AuthService>()
    private val loginUseCase = LoginUseCase(authService)

    private fun fakeUserInfo(role: Role = Role.CLIENT) = UserInfo(
        id = "test-id",
        userName = "testuser",
        password = "hashed",
        fullname = "Test User",
        role = role,
        roles = listOf("Cliente"),
        active = true,
        currentDate = "2025-01-15"
    )

    @Test
    fun `execute returns UserInfo from auth service`() {
        val expected = fakeUserInfo()
        whenever(authService.login("testuser", "password123", "Cliente")).thenReturn(expected)

        val result = loginUseCase.execute("testuser", "password123", "Cliente", mock())

        assertThat(result).isEqualTo(expected)
        assertThat(result.userName).isEqualTo("testuser")
    }

    @Test
    fun `execute sets USER_INFO session attribute`() {
        val ctx = mock<Context>()
        val expected = fakeUserInfo()
        whenever(authService.login("testuser", "pass", "Cliente")).thenReturn(expected)

        loginUseCase.execute("testuser", "pass", "Cliente", ctx)

        verify(ctx).sessionAttribute("USER_INFO", expected)
    }

    @Test
    fun `execute propagates exception from auth service`() {
        whenever(authService.login("wrong", "wrong", "Cliente"))
            .thenThrow(io.javalin.http.UnauthorizedResponse("Invalid credentials"))

        org.junit.jupiter.api.assertThrows<io.javalin.http.UnauthorizedResponse> {
            loginUseCase.execute("wrong", "wrong", "Cliente", mock())
        }
    }
}
