package team.dreamapp.com.domain.usecase.auth

import team.dreamapp.com.domain.services.auth.AuthorizationService
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.domain.entity.auth.UserInfo

class AuthorizeAccessUseCase(private val authorizationService: AuthorizationService) {
    fun execute(userInfo: UserInfo?, permittedRoles: List<Role>): Boolean {
        return authorizationService.isAuthorized(userInfo, permittedRoles)
    }
}