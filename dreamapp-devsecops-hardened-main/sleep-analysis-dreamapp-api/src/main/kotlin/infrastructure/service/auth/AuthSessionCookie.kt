package team.dreamapp.com.infrastructure.service.auth

import io.javalin.http.Context

object AuthSessionCookie {
    const val MAX_AGE_SECONDS = 12 * 60 * 60
    private const val PRODUCTION_COOKIE_NAME = "__Host-dreamapp_session"
    private const val DEVELOPMENT_COOKIE_NAME = "dreamapp_session"

    fun read(ctx: Context): String? = ctx.cookie(name())

    fun set(ctx: Context, token: String) {
        ctx.header("Set-Cookie", serialize(token, MAX_AGE_SECONDS))
    }

    fun clear(ctx: Context) {
        ctx.header("Set-Cookie", serialize("", 0))
    }

    fun name(): String = if (isSecure()) PRODUCTION_COOKIE_NAME else DEVELOPMENT_COOKIE_NAME

    internal fun serialize(value: String, maxAge: Int): String = buildString {
        append(name()).append('=').append(value)
        append("; Path=/; Max-Age=").append(maxAge)
        append("; HttpOnly")
        if (isSecure()) {
            append("; Secure; SameSite=None; Partitioned")
        } else {
            append("; SameSite=Lax")
        }
    }

    private fun isSecure(): Boolean = System.getenv("SESSION_COOKIE_SECURE")
        ?.toBooleanStrictOrNull()
        ?: ((System.getenv("ENVIRONMENT") ?: "production") == "production")
}
