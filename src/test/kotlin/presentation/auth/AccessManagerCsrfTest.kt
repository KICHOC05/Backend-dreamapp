package team.dreamapp.com.presentation.auth

import io.javalin.http.ForbiddenResponse
import io.javalin.http.Handler
import io.javalin.http.HandlerType
import io.javalin.http.UnauthorizedResponse
import io.javalin.router.Endpoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService

class AccessManagerCsrfTest {

    private fun mockContext(
        method: HandlerType = HandlerType.GET,
        path: String = "/api/test",
        authorization: String? = null,
        origin: String? = null,
        referer: String? = null,
        xRequestedWith: String? = null,
        matchedPath: String = "/api/test"
    ): io.javalin.http.Context {
        val ctx = mock<io.javalin.http.Context>()
        val endpoint = Endpoint(method, matchedPath, Handler { })
        whenever(ctx.method()).thenReturn(method)
        whenever(ctx.path()).thenReturn(path)
        whenever(ctx.endpoint()).thenReturn(endpoint)
        whenever(ctx.header("Authorization")).thenReturn(authorization)
        whenever(ctx.header("Origin")).thenReturn(origin)
        whenever(ctx.header("Referer")).thenReturn(referer)
        whenever(ctx.header("X-Requested-With")).thenReturn(xRequestedWith)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.CLIENT))
        whenever(ctx.ip()).thenReturn("127.0.0.1")

        authorization?.let {
            val token = it.removePrefix("Bearer ").trim()
            val user = AuthTokenService.resolve(token)
            whenever(ctx.sessionAttribute<UserInfo>("USER_INFO")).thenReturn(user)
        }

        return ctx
    }

    @Test
    fun `CSRF validation passes when Origin header is present`() {
        val ctx = mockContext(method = HandlerType.POST, origin = "https://example.com")
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `CSRF validation passes when Referer header is present`() {
        val ctx = mockContext(method = HandlerType.POST, referer = "https://example.com/page")
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `CSRF validation passes when X-Requested-With header is present`() {
        val ctx = mockContext(method = HandlerType.POST, xRequestedWith = "XMLHttpRequest")
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `CSRF validation fails when no Origin Referer or X-Requested-With`() {
        val ctx = mockContext(method = HandlerType.POST)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        assertThrows<ForbiddenResponse> {
            AccessManager.handleAccess(ctx)
        }
    }

    @Test
    fun `GET requests skip CSRF validation`() {
        val ctx = mockContext(method = HandlerType.GET)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        AccessManager.handleAccess(ctx)
    }

    @Test
    fun `DELETE requests require CSRF validation`() {
        val ctx = mockContext(method = HandlerType.DELETE)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        assertThrows<ForbiddenResponse> {
            AccessManager.handleAccess(ctx)
        }
    }

    @Test
    fun `authenticated user without token gets UnauthorizedResponse`() {
        val ctx = mockContext(method = HandlerType.GET)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.CLIENT))
        whenever(ctx.sessionAttribute<UserInfo>("USER_INFO")).thenReturn(null)

        assertThrows<UnauthorizedResponse> {
            AccessManager.handleAccess(ctx)
        }
    }

    @Test
    fun `PATCH requests require CSRF validation`() {
        val ctx = mockContext(method = HandlerType.PATCH)
        whenever(ctx.routeRoles()).thenReturn(setOf(Role.UNAUTHENTICATED))

        assertThrows<ForbiddenResponse> {
            AccessManager.handleAccess(ctx)
        }
    }
}
