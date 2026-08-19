package team.dreamapp.com.domain.entity.sleep

import java.time.LocalDateTime

/**
 * Represents a sleep state change event
 */
data class SleepStateEvent(
    val userId: String,
    val userName: String,
    val sleepState: SleepState,
    val timestamp: LocalDateTime,
    val deviceId: String? = null,
    val location: Location? = null
) {
    /**
     * Convert to a map for JSON serialization
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "userName" to userName,
            "sleepState" to sleepState.name,
            "sleepStateDisplay" to sleepState.displayName,
            "colorCode" to sleepState.colorCode,
            "timestamp" to timestamp.toString(),
            "deviceId" to (deviceId ?: ""),
            "location" to (location?.toMap() ?: emptyMap<String, Any>())
        )
    }
}

/**
 * Represents a geographical location
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "address" to (address ?: "")
        )
    }
}
