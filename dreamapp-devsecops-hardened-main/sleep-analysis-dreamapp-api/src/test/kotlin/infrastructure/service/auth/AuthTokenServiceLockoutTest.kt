package team.dreamapp.com.infrastructure.service.auth

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.domain.entity.auth.UserInfo

class AuthTokenServiceLockoutTest {

    private fun fakeUserInfo(userName: String = "lockoutuser") = UserInfo(
        id = "test-id",
        userName = userName,
        password = "secret123",
        fullname = "Test User",
        roles = listOf("Cliente"),
        active = true,
        currentDate = "2025-01-15"
    )

    @Test
    fun `isLockedOut returns false for unknown user`() {
        assertThat(AuthTokenService.isLockedOut("unknown")).isFalse()
    }

    @Test
    fun `isLockedOut returns false after fewer than 5 failed attempts`() {
        val user = "partialfail"
        repeat(4) {
            AuthTokenService.recordFailedAttempt(user)
        }
        assertThat(AuthTokenService.isLockedOut(user)).isFalse()
    }

    @Test
    fun `isLockedOut returns true after 5 failed attempts`() {
        val user = "lockouttarget"
        repeat(4) {
            AuthTokenService.recordFailedAttempt(user)
        }
        val locked = AuthTokenService.recordFailedAttempt(user)
        assertThat(locked).isTrue()
        assertThat(AuthTokenService.isLockedOut(user)).isTrue()
    }

    @Test
    fun `clearFailedAttempts resets lockout`() {
        val user = "clearme"
        repeat(5) {
            AuthTokenService.recordFailedAttempt(user)
        }
        assertThat(AuthTokenService.isLockedOut(user)).isTrue()

        AuthTokenService.clearFailedAttempts(user)
        assertThat(AuthTokenService.isLockedOut(user)).isFalse()
    }

    @Test
    fun `getFailedAttemptCount returns 0 for unknown user`() {
        assertThat(AuthTokenService.getFailedAttemptCount("nobody")).isEqualTo(0)
    }

    @Test
    fun `getFailedAttemptCount increments with each failed attempt`() {
        val user = "counter"
        AuthTokenService.recordFailedAttempt(user)
        assertThat(AuthTokenService.getFailedAttemptCount(user)).isEqualTo(1)
        AuthTokenService.recordFailedAttempt(user)
        assertThat(AuthTokenService.getFailedAttemptCount(user)).isEqualTo(2)
    }

    @Test
    fun `issue clears failed attempts for user`() {
        val user = "retryuser"
        repeat(3) {
            AuthTokenService.recordFailedAttempt(user)
        }
        assertThat(AuthTokenService.getFailedAttemptCount(user)).isEqualTo(3)

        AuthTokenService.issue(fakeUserInfo(user))
        assertThat(AuthTokenService.getFailedAttemptCount(user)).isEqualTo(0)
    }

    @Test
    fun `different users have separate lockout state`() {
        val user1 = "user_alpha"
        val user2 = "user_beta"

        repeat(5) { AuthTokenService.recordFailedAttempt(user1) }
        assertThat(AuthTokenService.isLockedOut(user1)).isTrue()
        assertThat(AuthTokenService.isLockedOut(user2)).isFalse()
    }
}
