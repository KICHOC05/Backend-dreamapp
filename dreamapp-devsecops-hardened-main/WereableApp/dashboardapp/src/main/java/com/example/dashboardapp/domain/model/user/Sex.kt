package com.example.dashboardapp.domain.model.user

enum class Sex(val value: String) {
    MEN("men"),
    WOMAN("woman");

    companion object {
        fun fromString(value: String): Sex = when(value.lowercase()) {
            "men", "m" -> MEN
            "woman", "w" -> WOMAN
            else -> MEN
        }
    }
}
