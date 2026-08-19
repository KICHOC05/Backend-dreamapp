package com.example.dashboardapp.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.dashboardapp.data.local.session.SessionManager

data class LogoutUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val success: Boolean = false
)

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutUiState())
    val uiState: StateFlow<LogoutUiState> = _uiState

    fun logout() {
        android.util.Log.d("LogoutViewModel", "logout() llamado")
        _uiState.value = LogoutUiState(isLoading = true)

        viewModelScope.launch {
            android.util.Log.d("LogoutViewModel", "Llamando LogoutUseCase...")
            val result = logoutUseCase()

            if (result.isSuccess) {
                val response = result.getOrNull()
                if (response?.success == true) {
                    android.util.Log.d("LogoutViewModel", "Logout exitoso, success=true")
                    sessionManager.clearSession()
                    _uiState.value = LogoutUiState(success = true)
                } else {
                    android.util.Log.d("LogoutViewModel", "Logout no fue exitoso (success!=true)")
                    _uiState.value = LogoutUiState(error = "Logout no fue exitoso")
                }
            } else {
                android.util.Log.d("LogoutViewModel", "Logout falló: ${result.exceptionOrNull()?.message}")
                _uiState.value = LogoutUiState(
                    error = result.exceptionOrNull()?.message ?: "Error desconocido"
                )
            }
        }
    }
}