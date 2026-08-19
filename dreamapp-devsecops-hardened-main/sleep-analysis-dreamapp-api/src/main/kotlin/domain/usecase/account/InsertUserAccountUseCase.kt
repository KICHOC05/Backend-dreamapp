package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.repository.account.UserAccountRepository

class InsertUserAccountUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(userAccount: UserAccount): String {
        return repository.insert(userAccount)
    }
}