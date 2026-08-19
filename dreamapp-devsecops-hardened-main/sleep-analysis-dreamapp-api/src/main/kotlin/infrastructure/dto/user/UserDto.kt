package team.dreamapp.com.infrastructure.dto.user

data class UserDto(
    val uidUser: String,
    val username: String,
    val weightKg: Int,
    val heightCm: Int,
    val age: Int,
    val sex: String,
    val profilePictureUrl: String
)