package com.example.appmobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.domain.usecase.RegisterUserUseCase
import com.example.appmobile.data.database.UserDatabase
import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.presentation.communication.WearMessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState
    
    private val registerUserUseCase = RegisterUserUseCase()
    private val userDatabase = UserDatabase.getDatabase(context)
    private val wearMessageSender = WearMessageSender(context)

    fun registerUser(
        uidUser: String,
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: String
    ) {
        _uiState.value = RegisterUiState(isLoading = true)
        viewModelScope.launch {
            try {
                // Registrar en la nube
                val result = registerUserUseCase(
                    uidUser = uidUser,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    sex = sex
                )
                
                if (result.isSuccess) {
                    // Si el registro en la nube fue exitoso, guardar localmente
                    launch(Dispatchers.IO) {
                        try {
                            // Crear entidad para la base de datos local
                            val userEntity = UserDataEntity(
                                edad = age,
                                peso = weightKg.toFloat(),
                                estatura = heightCm.toFloat(),
                                sexo = sex
                            )
                            
                            // Guardar en base de datos local
                            val id = userDatabase.userDataDao().insert(userEntity)
                            val userWithId = userEntity.copy(id = id)
                            
                            // Enviar al wearable
                            wearMessageSender.sendUserDataToWear(
                                edad = age,
                                peso = weightKg.toFloat(),
                                estatura = heightCm.toFloat(),
                                sexo = sex
                            )
                            
                            Log.d("RegisterViewModel", "✅ Usuario guardado localmente y enviado al wearable: $userWithId")
                        } catch (e: Exception) {
                            Log.e("RegisterViewModel", "❌ Error al guardar localmente o enviar al wearable: ${e.message}")
                        }
                    }
                    
                    _uiState.value = RegisterUiState(isSuccess = true)
                } else {
                    _uiState.value = RegisterUiState(
                        error = result.exceptionOrNull()?.message ?: "Error en el registro"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState(
                    error = e.message ?: "Error inesperado"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}
