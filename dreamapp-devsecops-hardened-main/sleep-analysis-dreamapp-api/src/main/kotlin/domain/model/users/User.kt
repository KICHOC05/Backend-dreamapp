package team.dreamapp.com.domain.model.users

data class User(
    private val id: String?,
    private val name: String?,
    private val weight: Int,
    private val height: Int,
    private val age: Int,
    private val sex: Sex,
    private val pictureUrl: String?
)