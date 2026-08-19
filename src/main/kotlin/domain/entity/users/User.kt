package team.dreamapp.com.domain.entity.users

data class User(
    val id: String?,
    val name: String?,
    val weight: Int,
    val height: Int,
    val age: Int,
    val sex: String?,
    val pictureUrl: String?
)