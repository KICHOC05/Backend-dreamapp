package team.dreamapp.com.infrastructure.service.auth

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.domain.entity.auth.UserInfo

class AuthTokenServiceTest {

    private fun fakeUserInfo() = UserInfo(
        id = "test-id",
        userName = "testuser",
        password = "secret123",
        fullname = "Test User",
        roles = listOf("Cliente"),
        active = true,
        currentDate = "2025-01-15"
    )

    @Test
    fun `issue returns a non-blank token`() {
        val token = AuthTokenService.issue(fakeUserInfo())
        assertThat(token).isNotBlank()
        assertThat(token.length).isGreaterThan(20)
    }

    @Test
    fun `resolve returns user info for valid token`() {
        val user = fakeUserInfo()
        val token = AuthTokenService.issue(user)

        val resolved = AuthTokenService.resolve(token)

        assertThat(resolved).isNotNull()
        assertThat(resolved!!.id).isEqualTo(user.id)
        assertThat(resolved.userName).isEqualTo(user.userName)
    }

    @Test
    fun `resolve returns null for invalid token`() {
        val resolved = AuthTokenService.resolve("invalid-token-abc123")
        assertThat(resolved).isNull()
    }

    @Test
    fun `resolve returns null for blank token`() {
        assertThat(AuthTokenService.resolve("")).isNull()
        assertThat(AuthTokenService.resolve(null)).isNull()
    }

    @Test
    fun `revoke invalidates token`() {
        val token = AuthTokenService.issue(fakeUserInfo())
        assertThat(AuthTokenService.resolve(token)).isNotNull()

        AuthTokenService.revoke(token)

        assertThat(AuthTokenService.resolve(token)).isNull()
    }

    @Test
    fun `issued token masks password`() {
        val user = fakeUserInfo()
        val token = AuthTokenService.issue(user)

        val resolved = AuthTokenService.resolve(token)

        assertThat(resolved!!.password).isEqualTo("**************")
    }

    @Test
    fun `different calls produce different tokens`() {
        val token1 = AuthTokenService.issue(fakeUserInfo())
        val token2 = AuthTokenService.issue(fakeUserInfo())
        assertThat(token1).isNotEqualTo(token2)
    }
}
