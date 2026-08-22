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

    private data class FailedAttempt(val count: Int, val firstAttemptAt: Long, val lockedUntil: Long)
    private val failedAttempts = ConcurrentHashMap<String, FailedAttempt>()
    private const val maxFailedAttempts = 5
    private const val lockoutDurationSeconds = 15 * 60L
    private const val attemptWindowSeconds = 15 * 60L

    fun issue(user: UserInfo): String {
        cleanup()
        failedAttempts.remove(user.userName.lowercase())
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

    fun isLockedOut(userName: String): Boolean {
        val key = userName.lowercase()
        val attempt = failedAttempts[key] ?: return false
        val now = Instant.now().epochSecond
        if (attempt.lockedUntil > now) return true
        if (now - attempt.firstAttemptAt > attemptWindowSeconds) {
            failedAttempts.remove(key)
            return false
        }
        return false
    }

    fun recordFailedAttempt(userName: String): Boolean {
        val key = userName.lowercase()
        val now = Instant.now().epochSecond
        failedAttempts.compute(key) { _, existing ->
            if (existing == null || now - existing.firstAttemptAt > attemptWindowSeconds) {
                FailedAttempt(1, now, 0L)
            } else if (existing.count + 1 >= maxFailedAttempts) {
                FailedAttempt(existing.count + 1, existing.firstAttemptAt, now + lockoutDurationSeconds)
            } else {
                existing.copy(count = existing.count + 1)
            }
        }
        return isLockedOut(userName)
    }

    fun clearFailedAttempts(userName: String) {
        failedAttempts.remove(userName.lowercase())
    }

    fun getFailedAttemptCount(userName: String): Int {
        return failedAttempts[userName.lowercase()]?.count ?: 0
    }

    private fun cleanup() {
        val now = Instant.now().epochSecond
        sessions.entries.removeIf { it.value.expiresAt <= now }
        failedAttempts.entries.removeIf { now - it.value.firstAttemptAt > attemptWindowSeconds && it.value.lockedUntil <= now }
    }

    private fun hash(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
