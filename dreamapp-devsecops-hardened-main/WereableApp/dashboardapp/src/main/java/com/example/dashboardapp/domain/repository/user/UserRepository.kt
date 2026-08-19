package com.example.dashboardapp.domain.repository.user

import com.example.dashboardapp.domain.model.user.User

interface UserRepository {
    suspend fun getAllUsers(): Result<List<User>>
    fun observeUsersWebSocket(url: String): kotlinx.coroutines.flow.Flow<List<User>>
}