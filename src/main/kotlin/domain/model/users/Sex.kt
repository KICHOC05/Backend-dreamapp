package team.dreamapp.com.domain.model.users

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Sex {
    MEN, WOMAN;

    @JsonValue
    fun toValue(): String = when(this) {
        MEN -> "men"
        WOMAN -> "woman"
    }

    companion object {
        @JsonCreator
        fun fromString(value: String): Sex = when(value.lowercase()) {
            "men", "m" -> MEN
            "woman", "w" -> WOMAN
            else -> throw IllegalArgumentException("Invalid sex: $value")
        }
    }
}