package team.dreamapp.com.presentation.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.javalin.http.Context
import io.javalin.http.TooManyRequestsResponse
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RequestSecurityTest {

    private fun mockContext(ip: String = "127.0.0.1"): Context {
        val ctx = mock<Context>()
        whenever(ctx.ip()).thenReturn(ip)
        return ctx
    }

    @Test
    fun `apply sets security headers`() {
        val ctx = mockContext()

        RequestSecurity.apply(ctx)

        verify(ctx).header("X-Content-Type-Options", "nosniff")
        verify(ctx).header("X-Frame-Options", "DENY")
        verify(ctx).header("Referrer-Policy", "no-referrer")
        verify(ctx).header("Permissions-Policy", "geolocation=(), camera=(), microphone=()")
        verify(ctx).header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
        verify(ctx).header("Cache-Control", "no-store")
    }

    @Test
    fun `apply allows requests within rate limit`() {
        val ip = "10.1.1.1"
        repeat(5) {
            RequestSecurity.apply(mockContext(ip))
        }
    }

    @Test
    fun `apply throws TooManyRequestsResponse when rate limit exceeded`() {
        val ip = "10.2.2.2"
        repeat(121) {
            if (it < 120) {
                RequestSecurity.apply(mockContext(ip))
            }
        }
        assertThrows<TooManyRequestsResponse> {
            RequestSecurity.apply(mockContext(ip))
        }
    }

    @Test
    fun `different IPs have separate rate limits`() {
        val ip1 = "10.3.3.3"
        val ip2 = "10.4.4.4"
        repeat(120) {
            RequestSecurity.apply(mockContext(ip1))
        }
        // ip2 should still be able to make requests
        RequestSecurity.apply(mockContext(ip2))
    }
}
