package team.dreamapp.com.domain.model.sleep

enum class Quality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR;

    companion object {
        fun fromString(value: String?): Quality =
            values().find { it.name.equals(value, ignoreCase = true) } ?: POOR
    }
}