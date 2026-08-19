package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.entity.auth.UserInfo
import team.dreamapp.com.domain.repository.account.UserAccountRepository

class UserInfoByUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(type: String, param: String): UserInfo {
        return repository.userInfoBy(type, param)
    }
}