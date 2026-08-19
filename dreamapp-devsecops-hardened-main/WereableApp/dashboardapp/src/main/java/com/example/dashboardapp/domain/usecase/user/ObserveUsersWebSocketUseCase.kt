package com.example.dashboardapp.domain.usecase.user

import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.domain.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow

class ObserveUsersWebSocketUseCase(private val repository: UserRepository) {
    operator fun invoke(url: String): Flow<List<User>> = repository.observeUsersWebSocket(url)
}
