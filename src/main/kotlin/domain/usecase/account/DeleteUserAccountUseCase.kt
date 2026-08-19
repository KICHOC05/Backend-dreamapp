package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.repository.account.UserAccountRepository

class DeleteUserAccountUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(uuid: String): String {
        return repository.delete(uuid)
    }
}