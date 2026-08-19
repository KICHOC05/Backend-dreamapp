package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.repository.account.UserAccountRepository

class UpdateUserAccountUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(userAccount: UserAccount): String {
        return repository.update(userAccount)
    }
}