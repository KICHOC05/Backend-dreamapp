package team.dreamapp.com.presentation.security

import java.net.URI

object WebOriginPolicy {
    private val localDevelopmentOrigins = setOf(
        "http://localhost:5173",
        "http://127.0.0.1:5173"
    )

    val allowedOrigins: Set<String> by lazy {
        (System.getenv("ALLOWED_ORIGINS")
            ?.split(',')
            ?.map(String::trim)
            ?.filter(String::isNotEmpty)
            ?.toSet()
            ?: localDevelopmentOrigins)
            .mapNotNull(::normalizeOrigin)
            .toSet()
            .also { require(it.isNotEmpty()) { "ALLOWED_ORIGINS must contain at least one valid origin" } }
    }

    fun isAllowed(originOrReferer: String?): Boolean {
        val normalized = originOrReferer?.let(::normalizeOrigin) ?: return false
        return normalized in allowedOrigins
    }

    internal fun normalizeOrigin(value: String): String? = runCatching {
        val uri = URI(value.trim())
        require(uri.scheme == "http" || uri.scheme == "https")
        require(!uri.host.isNullOrBlank())
        require(uri.userInfo == null)
        val defaultPort = (uri.scheme == "http" && uri.port == 80) ||
            (uri.scheme == "https" && uri.port == 443)
        val port = if (uri.port == -1 || defaultPort) "" else ":${uri.port}"
        "${uri.scheme.lowercase()}://${uri.host.lowercase()}$port"
    }.getOrNull()
}
