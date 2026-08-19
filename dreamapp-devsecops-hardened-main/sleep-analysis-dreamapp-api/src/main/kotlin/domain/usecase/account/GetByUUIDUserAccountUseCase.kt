package team.dreamapp.com.domain.usecase.account

import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.repository.account.UserAccountRepository

class GetByUUIDUserAccountUseCase(private val repository: UserAccountRepository) {
    operator fun invoke(uuid: String): UserAccount? {
        return repository.getByUUID(uuid)
    }
}