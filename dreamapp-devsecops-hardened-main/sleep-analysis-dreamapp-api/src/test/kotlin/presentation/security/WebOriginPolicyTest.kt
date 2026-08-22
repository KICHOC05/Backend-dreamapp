package team.dreamapp.com.presentation.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WebOriginPolicyTest {

    @Test
    fun `normalization removes paths and default ports`() {
        assertThat(WebOriginPolicy.normalizeOrigin("https://Example.COM:443/path"))
            .isEqualTo("https://example.com")
    }

    @Test
    fun `normalization rejects non-http origins and user info`() {
        assertThat(WebOriginPolicy.normalizeOrigin("javascript:alert(1)")).isNull()
        assertThat(WebOriginPolicy.normalizeOrigin("https://user@example.com")).isNull()
    }

    @Test
    fun `local development origin is allowed by default`() {
        assertThat(WebOriginPolicy.isAllowed("http://localhost:5173/login")).isTrue()
        assertThat(WebOriginPolicy.isAllowed("https://attacker.example")).isFalse()
    }
}
