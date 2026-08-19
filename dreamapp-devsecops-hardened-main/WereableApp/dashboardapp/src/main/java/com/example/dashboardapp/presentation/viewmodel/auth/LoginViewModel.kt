package com.example.dashboardapp.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.dashboardapp.data.local.dao.UserDao
import kotlinx.coroutines.Dispatchers
import com.example.dashboardapp.data.local.session.SessionManager

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userSaved: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(userName: String, password: String, role: String) {
        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = loginUseCase(userName, password, role)
                if (result.isSuccess) {
                    val savedUser = kotlinx.coroutines.withContext(Dispatchers.IO) { userDao.getUser() }
                    if (savedUser != null) {
                        sessionManager.setLoggedIn(true)
                    }
                    _uiState.value = LoginUiState(
                        isSuccess = true,
                        userSaved = savedUser != null
                    )
                } else {
                    _uiState.value = LoginUiState(error = result.exceptionOrNull()?.message ?: "Error desconocido")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(error = e.message ?: "Error inesperado")
            }
        }
    }
}