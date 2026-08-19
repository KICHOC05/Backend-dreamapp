package team.dreamapp.com.presentation.dto.auth

data class LoginRequestDto(
    val userName: String = "",
    val password: String = "",
    val role: String = "Cliente"
)
