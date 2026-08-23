package team.dreamapp.com.domain.services.auth

import team.dreamapp.com.domain.entity.auth.UserInfo

interface AuthService {
    fun login(userName: String, password: String, role: String): UserInfo
    fun logout(): Boolean
}
