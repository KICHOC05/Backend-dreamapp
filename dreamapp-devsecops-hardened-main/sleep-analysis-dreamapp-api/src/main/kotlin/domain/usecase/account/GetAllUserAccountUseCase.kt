package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.repository.account.UserAccountRepository

class GetAllUserAccountUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(where: String) : List<UserAccount> {
        return repository.getAll(where)
    }
}