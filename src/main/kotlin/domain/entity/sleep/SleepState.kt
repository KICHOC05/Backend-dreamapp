package team.dreamapp.com.domain.entity.sleep

/**
 * Represents the different sleep states that can be tracked.
 * Each state has a display name and a color code for visualization.
 */
enum class SleepState(val displayName: String, val colorCode: String) {
    REM("REM", "#FF6B6B"),      // Red - REM sleep
    LIGHT("Light", "#4ECDC4"),   // Teal - Light sleep
    DEEP("Deep", "#45B7D1"),     // Blue - Deep sleep
    AWAKE("Awake", "#96CEB4");   // Green - Awake state

    companion object {
        /**
         * Get sleep state from string value, case insensitive
         */
        fun fromString(value: String): SleepState? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
