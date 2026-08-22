package team.dreamapp.com.infrastructure.service.auth

import org.slf4j.LoggerFactory

object AuditLogger {
    private val logger = LoggerFactory.getLogger("AUDIT")

    enum class Event {
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        REGISTER_ATTEMPT,
        REGISTER_SUCCESS,
        REGISTER_FAILURE,
        VERIFICATION_SUCCESS,
        VERIFICATION_FAILURE,
        VERIFICATION_CODE_SENT,
        UNAUTHORIZED_ACCESS,
        ROLE_VIOLATION,
        RATE_LIMIT_EXCEEDED,
        ACCOUNT_CREATED,
        ACCOUNT_UPDATED,
        ACCOUNT_DELETED,
        SUBSCRIPTION_CHANGED
    }

    fun log(
        event: Event,
        userId: String? = null,
        userName: String? = null,
        clientIp: String? = null,
        detail: String? = null
    ) {
        val message = buildMap {
            put("event", event.name)
            userId?.let { put("userId", it) }
            userName?.let { put("userName", it) }
            clientIp?.let { put("clientIp", it) }
            detail?.let { put("detail", it) }
            put("timestamp", System.currentTimeMillis())
        }
        logger.info(message.toString())
    }
}
