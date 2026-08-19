package team.dreamapp.com.domain.usecase.users

import team.dreamapp.com.domain.repository.users.UserRepository
import team.dreamapp.com.domain.entity.users.User

class GetAllUsersUseCase(
    private val repository: UserRepository
) {
    operator fun invoke(): List<User> = repository.getAllUsers()
}