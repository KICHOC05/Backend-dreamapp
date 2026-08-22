package team.dreamapp.com.domain.entity.auth

import io.javalin.http.BadRequestResponse
import java.io.Serializable

data class UserInfo(
    var id: String,
    val userName: String,
    var password: String,
    val fullname: String,
    var role: Role = Role.UNAUTHENTICATED,
    val roles: List<String> = listOf(),
    val active: Boolean = false,
    val currentDate: String): Serializable {
    /* Photo field */
    val photoUrl = "api/image?url=photos/users/$id-mini.jpg"

    /* Map from strRole to Role  */
    fun mapRole(strRole: String) {
        roles.firstOrNull { it.equals(strRole, true) }?.also {
            role = when(it.lowercase()) {
                "sysadmin" -> Role.SYSADMIN
                "admin" -> Role.ADMIN
                "cliente" -> Role.CLIENT
                else -> Role.UNAUTHENTICATED
            }
        } ?: throw BadRequestResponse("The user not have this permission")
        password = "**************"
    }
}