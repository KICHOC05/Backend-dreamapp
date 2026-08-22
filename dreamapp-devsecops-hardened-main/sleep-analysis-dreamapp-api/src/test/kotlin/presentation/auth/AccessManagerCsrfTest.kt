package team.dreamapp.com.presentation.auth

import io.javalin.http.ForbiddenResponse
import io.javalin.http.HandlerType
import io.javalin.http.UnauthorizedResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.infrastructure.service.auth.AuthSessionCookie

class AccessManagerCsrfTest {

    private fun mockContext(
        method: HandlerType = HandlerType.GET,
        authorization: String? = null,
        origin: String? = null,
        referer: String? = null,
        webRequestHeader: String? = null,
        secFetchSite: String? = null,
        cookieToken: String? = null
    ): io.javalin.http.Context {
        val ctx = mock<io.javalin.http.Context>()
        whenever(ctx.method()).thenReturn(method)
        whenever(ctx.path()).thenReturn("/api/test")
        whenever(ctx.matchedPath()).thenReturn("/api/test")
        whenever(ctx.header("Authorization")).thenReturn(authorization)
        whenever(ctx.header("Origin")).thenReturn(origin)
        whenever(ctx.header("Referer")).thenReturn(referer)
        whenever(ctx.header("X-DreamApp-Request")).thenReturn(webRequestHeader)
        whenever(ctx.header("Sec-Fetch-Site")).thenReturn(secFetchSite)
        whenever(ctx.cookie(AuthSessionCookie.name())).thenReturn(cookieToken)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))
        whenever(ctx.ip()).thenReturn("127.0.0.1")
        return ctx
    }

    @Test
    fun `browser POST passes with an allowed origin and custom header`() {
        val ctx = mockContext(
            method = HandlerType.POST,
            origin = "http://localhost:5173",
            webRequestHeader = "DreamAppWeb"
        )

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `browser POST accepts an allowed referer and custom header`() {
        val ctx = mockContext(
            method = HandlerType.POST,
            referer = "http://localhost:5173/register",
            webRequestHeader = "DreamAppWeb"
        )

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `browser POST rejects an untrusted origin`() {
        val ctx = mockContext(
            method = HandlerType.POST,
            origin = "https://attacker.example",
            webRequestHeader = "DreamAppWeb"
        )

        assertThrows<ForbiddenResponse> { AccessManager.handleAccess(ctx) }
    }

    @Test
    fun `browser POST rejects a missing custom header`() {
        val ctx = mockContext(method = HandlerType.POST, origin = "http://localhost:5173")

        assertThrows<ForbiddenResponse> { AccessManager.handleAccess(ctx) }
    }

    @Test
    fun `cookie authenticated mutation rejects missing browser proof`() {
        val ctx = mockContext(method = HandlerType.PATCH, cookieToken = "opaque-token")

        assertThrows<ForbiddenResponse> { AccessManager.handleAccess(ctx) }
    }

    @Test
    fun `browser metadata without an origin is rejected`() {
        val ctx = mockContext(method = HandlerType.DELETE, secFetchSite = "cross-site")

        assertThrows<ForbiddenResponse> { AccessManager.handleAccess(ctx) }
    }

    @Test
    fun `non browser clients remain compatible without CSRF headers`() {
        val ctx = mockContext(method = HandlerType.POST)

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `bearer clients do not require browser CSRF headers`() {
        val ctx = mockContext(method = HandlerType.POST, authorization = "Bearer opaque-token")

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `GET requests skip CSRF validation`() {
        val ctx = mockContext(method = HandlerType.GET, origin = "https://attacker.example")

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `authenticated route without a valid token is unauthorized`() {
        val ctx = mockContext(method = HandlerType.GET)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.CLIENT))

        assertThrows<UnauthorizedResponse> { AccessManager.handleAccess(ctx) }
    }
}
