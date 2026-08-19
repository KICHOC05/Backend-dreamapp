package team.dreamapp.com.domain.entity.account

import team.dreamapp.com.domain.entity.auth.Role

data class UserAccount(
    var id: String = "",
    var userName: String = "",
    var password: String = "****************",
    var role: Role? = null,
    val roles: List<String> = listOf(),
    var firstName: String = "",
    var lastName: String = "",
    val mobilePhone: String? = null,
    val phoneOffice: String? = null,
    val phoneExt: String? = null,
    val email: String? = null,
    val active: Boolean = true
) {
    /* Photo field */
    val photoUrl = "/api/image?url=photos/users/$id-mini.jpg"

    /* List of Roles to String */
    fun rolesToStr() = roles.joinToString(",")
}