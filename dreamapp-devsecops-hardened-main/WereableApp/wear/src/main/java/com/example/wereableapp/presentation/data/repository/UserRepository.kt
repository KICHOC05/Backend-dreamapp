package com.example.wereableapp.presentation.data.repository

import com.example.wereableapp.presentation.domain.model.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object UserRepository {
    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData

    fun saveUserData(data: UserData) {
        _userData.value = data
    }
}
