package team.dreamapp.com.presentation.security

import io.javalin.http.Context
import io.javalin.http.TooManyRequestsResponse
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object RequestSecurity {
    private data class Window(var startedAt: Long, var requests: Int)
    private val windows = ConcurrentHashMap<String, Window>()
    private val limit = System.getenv("RATE_LIMIT_PER_MINUTE")?.toIntOrNull()?.coerceIn(10, 10_000) ?: 120

    fun apply(ctx: Context) {
        ctx.header("X-Content-Type-Options", "nosniff")
        ctx.header("X-Frame-Options", "DENY")
        ctx.header("Referrer-Policy", "no-referrer")
        ctx.header("Permissions-Policy", "geolocation=(), camera=(), microphone=()")
        ctx.header("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'")
        ctx.header("Cache-Control", "no-store")
        if ((System.getenv("ENVIRONMENT") ?: "production") == "production") {
            ctx.header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
        }

        val now = Instant.now().epochSecond
        val window = windows.compute(ctx.ip()) { _, current ->
            if (current == null || now - current.startedAt >= 60) Window(now, 1)
            else current.apply { requests++ }
        }!!
        if (window.requests > limit) throw TooManyRequestsResponse("Rate limit exceeded")
        if (windows.size > 10_000) windows.entries.removeIf { now - it.value.startedAt >= 60 }
    }
}
