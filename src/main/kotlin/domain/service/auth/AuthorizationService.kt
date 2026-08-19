package team.dreamapp.com.domain.services.auth

import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo

interface AuthorizationService {
    fun isAuthorized(userInfo: UserInfo?, permittedRoles: List<Role>): Boolean
}