package com.example.appmobile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.database.dao.UserDataDao
import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.presentation.communication.WearMessageSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.util.Log
import com.example.appmobile.data.remote.api.UserApi
import com.example.appmobile.data.remote.model.UserDataRequest

class UserViewModel(
    private val userDataDao: UserDataDao,
    private val wearMessageSender: WearMessageSender,
    private val userApi: UserApi // ← Inyectamos API
) : ViewModel() {

    private val _userSaved = MutableStateFlow(false)
    val userSaved: StateFlow<Boolean> = _userSaved

    fun saveUserData(uid: String, user: UserDataEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1️⃣ Guardar en SQLite
                val id = userDataDao.insert(user)
                val userWithId = user.copy(id = id)

                val request = UserDataRequest(
                    uidUser = uid,  // <-- Aquí usas el uid recibido como argumento
                    weightKg = user.peso,
                    heightCm = user.estatura,
                    age = user.edad,
                    sex = user.sexo
                )

                // 2️⃣ Enviar al API (Firebase o backend)
                try {
                    val response = userApi.registerUser(request) // tu endpoint POST
                    if (response.isSuccessful) {
                        Log.d("UserViewModel", "✅ Usuario registrado en API")
                    } else {
                        Log.d("UserViewModel", "⚠️ Falló registro en API: ${response.code()}")
                    }
                } catch (apiError: Exception) {
                    Log.d("UserViewModel", "❌ Error al registrar en API: ${apiError.localizedMessage}")
                }

                // 3️⃣ Enviar al wearable
                wearMessageSender.sendUserDataToWear(
                    edad = user.edad,
                    peso = user.peso,
                    estatura = user.estatura,
                    sexo = user.sexo
                )

                // 4️⃣ Notificar que se guardó
                _userSaved.value = true
                Log.d("UserViewModel", "✅ Usuario guardado local y enviado a wearable: $userWithId")

            } catch (e: Exception) {
                Log.d("UserViewModel", "❌ Error: ${e.localizedMessage}")
            }
        }
    }
}
