package team.dreamapp.com.infrastructure.service.auth

import io.javalin.http.Context
import org.mindrot.jbcrypt.BCrypt
import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.domain.services.auth.AuthService
import team.dreamapp.com.infrastructure.di.RepositoryProvider

class AuthServiceImpl : AuthService {
    override fun login(userName: String, password: String, role: String): UserInfo {
        val user = RepositoryProvider.userAccountRepository.userInfoBy("username", userName)
        if (user == null) throw Exception("The account does not exist")
        if (!BCrypt.checkpw(password, user.password)) throw Exception("Incorrect password")
        if (!user.active) throw Exception("The user is not active")
        try {
            user.mapRole(role)
        } catch (e: Exception) {
            throw Exception("The user does not have this role")
        }
        user.password = "**************"
        return user
    }

    override fun logout(ctx: Context): Boolean {
        ctx.sessionAttribute("USER_INFO", null)
        return true
    }
}
