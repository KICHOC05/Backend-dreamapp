package com.example.dashboardapp.presentation.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.data.local.dao.UserDao
import com.example.dashboardapp.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val userSaved: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        userName: String,
        firstName: String,
        lastName: String,
        password: String,
        roles: List<String>,
        mobilePhone: String,
        phoneOffice: String,
        phoneExt: String,
        email: String,
        active: Boolean,
        role: String // necesario para autologin
    ) {
        _uiState.value = RegisterUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val result = registerUseCase(
                    userName,
                    firstName,
                    lastName,
                    password,
                    roles,
                    mobilePhone,
                    phoneOffice,
                    phoneExt,
                    email,
                    active,
                    role
                )
                if (result.isSuccess) {
                    val savedUser = kotlinx.coroutines.withContext(Dispatchers.IO) { userDao.getUser() }
                    _uiState.value = RegisterUiState(
                        isSuccess = true,
                        userSaved = savedUser != null
                    )
                } else {
                    _uiState.value = RegisterUiState(error = result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(error = e.message ?: "Error inesperado")
            }
        }
    }
}