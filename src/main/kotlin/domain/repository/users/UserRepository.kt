package team.dreamapp.com.domain.repository.users

import team.dreamapp.com.domain.entity.users.User

interface UserRepository {
    fun getAllUsers(): List<User>
}