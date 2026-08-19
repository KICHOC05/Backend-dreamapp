package com.example.appmobile.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.appmobile.domain.model.SignInResult
import com.example.appmobile.domain.model.SignInState
import com.example.appmobile.domain.usecase.GoogleAuthUiClient
import com.example.appmobile.domain.usecase.SearchUserUseCase
import com.example.appmobile.domain.usecase.GetUserByUidUseCase
import com.example.appmobile.data.database.UserDatabase
import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.presentation.communication.WearMessageSender
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.util.Log

class SignInViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val context: Context
): ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    private val searchUserUseCase = SearchUserUseCase()
    private val getUserByUidUseCase = GetUserByUidUseCase()
    private val userDatabase = UserDatabase.getDatabase(context)
    private val wearMessageSender = WearMessageSender(context)
    val isUserSignedIn = googleAuthUiClient.getSignedInUser()

    /**
     * Inicia el proceso de login con Google (Paso 1 del flujo)
     */
    fun signIn() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, signInError = null) }

            val intentSender = googleAuthUiClient.signIn()
            if (intentSender == null) {
                _state.update { it.copy(isLoading = false, signInError = "No se pudo iniciar sesión con Google") }
                return@launch
            }
            // Aquí deberías disparar el intentSender en la UI (Activity/Fragment)
            _state.update { it.copy(signInIntentSender = intentSender, isLoading = false) }
        }
    }

    /**
     * Procesa el resultado del login con Google (Paso 2 del flujo)
     * Usa la API searchUser para verificar si el usuario está registrado
     */
    fun onSignInResult(result: SignInResult) {
        _state.update {
            it.copy(
                uid = result.data?.userId,
                signInError = result.errorMessage
            )
        }

        result.data?.userId?.let { uid ->
            viewModelScope.launch {
                try {
                    _state.update { it.copy(isLoading = true) }
                    val searchResult = searchUserUseCase(uid)
                    
                    if (searchResult.isSuccess) {
                        val searchResponse = searchResult.getOrNull()
                        Log.d("SignInViewModel", "🔍 SearchUser response: status=${searchResponse?.status}, message=${searchResponse?.message}")
                        
                        if (searchResponse?.status == true) {
                            // Usuario ya está registrado - obtener sus datos de la nube
                            Log.d("SignInViewModel", "👤 Usuario existente encontrado, obteniendo datos...")
                            try {
                                val userDataResult = getUserByUidUseCase(uid)
                                if (userDataResult.isSuccess) {
                                    val userDataResponse = userDataResult.getOrNull()
                                    val userData = userDataResponse?.data  // Acceder al objeto data
                                    Log.d("SignInViewModel", "📡 Datos obtenidos de la nube: response=$userDataResponse, userData=$userData")
                                    
                                    if (userData != null) {
                                        // Validar que los datos no sean null/empty
                                        if (userData.sex.isBlank() || userData.uidUser.isBlank()) {
                                            Log.e("SignInViewModel", "❌ Datos incompletos recibidos de la API: sex=${userData.sex}, uidUser=${userData.uidUser}")
                                            return@launch
                                        }
                                        
                                        // Guardar datos en base de datos local
                                        launch(Dispatchers.IO) {
                                            try {
                                                val userEntity = UserDataEntity(
                                                    edad = userData.age,
                                                    peso = userData.weightKg.toFloat(),
                                                    estatura = userData.heightCm.toFloat(),
                                                    sexo = userData.sex
                                                )
                                                
                                                Log.d("SignInViewModel", "💾 Guardando en BD local: $userEntity")
                                                val id = userDatabase.userDataDao().insert(userEntity)
                                                val userWithId = userEntity.copy(id = id)
                                                
                                                // Enviar al wearable
                                                Log.d("SignInViewModel", "⌚ Enviando datos al wearable...")
                                                wearMessageSender.sendUserDataToWear(
                                                    edad = userData.age,
                                                    peso = userData.weightKg.toFloat(),
                                                    estatura = userData.heightCm.toFloat(),
                                                    sexo = userData.sex
                                                )
                                                
                                                Log.d("SignInViewModel", "✅ Datos de usuario existente guardados localmente y enviados al wearable: $userWithId")
                                            } catch (e: Exception) {
                                                Log.e("SignInViewModel", "❌ Error guardando datos del usuario existente: ${e.message}")
                                                e.printStackTrace()
                                            }
                                        }
                                    } else {
                                        Log.e("SignInViewModel", "❌ userData es null después de obtener de la API")
                                    }
                                } else {
                                    Log.e("SignInViewModel", "❌ Error obteniendo datos del usuario: ${userDataResult.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                Log.e("SignInViewModel", "❌ Error obteniendo datos del usuario: ${e.message}")
                                e.printStackTrace()
                            }
                            
                            // Usuario ya está registrado - puede ir a ProfileScreen
                            _state.update { 
                                it.copy(
                                    isNewUser = false,
                                    isLoading = false,
                                    isSignInSuccessful = true,
                                    signInSuccessMessage = "¡Inicio de sesión exitoso! Bienvenido de vuelta."
                                ) 
                            }
                        } else {
                            // Usuario no está registrado - debe ir a UserScreen
                            _state.update { 
                                it.copy(
                                    isNewUser = true,
                                    isLoading = false,
                                    isSignInSuccessful = true,
                                    signInSuccessMessage = "¡Inicio de sesión exitoso! Por favor completa tu registro."
                                ) 
                            }
                        }
                    } else {
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                signInError = searchResult.exceptionOrNull()?.message ?: "Error verificando usuario"
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            signInError = e.message
                        ) 
                    }
                }
            }
        } ?: run {
            // Si no hay UID, marcar como error
            _state.update { 
                it.copy(
                    signInError = "No se pudo obtener el ID del usuario"
                )
            }
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }

    fun clearSuccessMessage() {
        _state.update { it.copy(signInSuccessMessage = null) }
    }
}