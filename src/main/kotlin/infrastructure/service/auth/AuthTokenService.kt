package team.dreamapp.com.infrastructure.service.auth

import team.dreamapp.com.domain.entity.auth.UserInfo
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

object AuthTokenService {
    private data class Session(val user: UserInfo, val expiresAt: Long)
    private val random = SecureRandom()
    private val sessions = ConcurrentHashMap<String, Session>()
    private const val lifetimeSeconds = 12 * 60 * 60L

    fun issue(user: UserInfo): String {
        cleanup()
        val raw = ByteArray(32).also(random::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw)
        sessions[hash(token)] = Session(user.copy(password = "**************"), Instant.now().epochSecond + lifetimeSeconds)
        return token
    }

    fun resolve(token: String?): UserInfo? {
        if (token.isNullOrBlank()) return null
        val session = sessions[hash(token)] ?: return null
        if (session.expiresAt <= Instant.now().epochSecond) {
            sessions.remove(hash(token))
            return null
        }
        return session.user
    }

    fun revoke(token: String?) {
        if (!token.isNullOrBlank()) sessions.remove(hash(token))
    }

    private fun cleanup() {
        val now = Instant.now().epochSecond
        sessions.entries.removeIf { it.value.expiresAt <= now }
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
