package team.dreamapp.com.infrastructure.service.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthSessionCookieTest {

    @Test
    fun `production cookie is HttpOnly secure cross-site and partitioned`() {
        val cookie = AuthSessionCookie.serialize("opaque-token", AuthSessionCookie.MAX_AGE_SECONDS)

        assertThat(cookie).startsWith("__Host-dreamapp_session=opaque-token")
        assertThat(cookie).contains("Path=/")
        assertThat(cookie).contains("HttpOnly")
        assertThat(cookie).contains("Secure")
        assertThat(cookie).contains("SameSite=None")
        assertThat(cookie).contains("Partitioned")
        assertThat(cookie).doesNotContain("Domain=")
    }

    @Test
    fun `cleared cookie expires immediately without exposing a token`() {
        val cookie = AuthSessionCookie.serialize("", 0)

        assertThat(cookie).contains("Max-Age=0")
        assertThat(cookie).contains("HttpOnly")
        assertThat(cookie).doesNotContain("opaque-token")
    }
}
