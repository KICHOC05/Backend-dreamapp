package team.dreamapp.com.domain.usecase.auth

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.domain.services.auth.AuthorizationService

class AuthorizeAccessUseCaseTest {

    private val authorizationService = mock<AuthorizationService>()
    private val useCase = AuthorizeAccessUseCase(authorizationService)

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
    fun `execute returns true when authorized`() {
        val user = fakeUserInfo(Role.ADMIN)
        whenever(authorizationService.isAuthorized(user, listOf(Role.ADMIN, Role.SYSADMIN))).thenReturn(true)

        val result = useCase.execute(user, listOf(Role.ADMIN, Role.SYSADMIN))

        assertThat(result).isTrue()
    }

    @Test
    fun `execute returns false when not authorized`() {
        val user = fakeUserInfo(Role.CLIENT)
        whenever(authorizationService.isAuthorized(user, listOf(Role.ADMIN))).thenReturn(false)

        val result = useCase.execute(user, listOf(Role.ADMIN))

        assertThat(result).isFalse()
    }

    @Test
    fun `execute returns false for null user`() {
        whenever(authorizationService.isAuthorized(null, listOf(Role.ADMIN))).thenReturn(false)

        val result = useCase.execute(null, listOf(Role.ADMIN))

        assertThat(result).isFalse()
    }
}
