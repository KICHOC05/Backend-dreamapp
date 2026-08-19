package team.dreamapp.com.domain.services.auth

import io.javalin.http.Context
import team.dreamapp.com.domain.entity.auth.UserInfo

interface AuthService {
    fun login(userName: String, password: String, role: String): UserInfo
    fun logout(ctx: Context): Boolean
}