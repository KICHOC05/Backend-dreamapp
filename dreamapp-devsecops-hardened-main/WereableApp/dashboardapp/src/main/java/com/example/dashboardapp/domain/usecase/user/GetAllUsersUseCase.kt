package com.example.dashboardapp.domain.usecase.user

import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.domain.repository.user.UserRepository

class GetAllUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<List<User>> {
        return repository.getAllUsers()
    }
}