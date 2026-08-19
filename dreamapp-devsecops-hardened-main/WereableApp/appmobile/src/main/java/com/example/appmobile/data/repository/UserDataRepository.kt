package com.example.appmobile.data.repository

import com.example.appmobile.data.database.dao.UserDataDao
import com.example.appmobile.data.database.entity.UserDataEntity
import com.example.appmobile.data.mapper.toRequest
import com.example.appmobile.data.remote.api.UserApi
import com.example.appmobile.data.remote.model.UserDataResponse

class UserDataRepository(
    private val api: UserApi,
    private val dao: UserDataDao,
) {
    suspend fun searchUser(uid: String) = api.searchUser(uid)

    // Obtiene el último registro localsuspend fun getUserByUid(uid: String) = api.getUserByUid(uid)
    suspend fun getUserByUid(uid: String): UserDataResponse? {
        val response = api.getUserByUid(uid)
        return if (response.isSuccessful) {
            response.body() // Esto ya es UserDataResponse
        } else {
            null
        }
    }

    suspend fun registerUser(uid: String, entity: UserDataEntity) =
        api.registerUser(entity.toRequest(uid))

    suspend fun saveLocal(entity: UserDataEntity) = dao.insert(entity)

    /**
     * Guarda los datos en Firebase usando la API y también en Room (offline).
     */
    suspend fun saveUserData(
        uid: String,
        edad: Int,
        peso: Float,
        estatura: Float,
        sexo: String
    ) {
        val entity = UserDataEntity(
            edad = edad,
            peso = peso,
            estatura = estatura,
            sexo = sexo
        )

        // Guardar en Firebase mediante la API
        registerUser(uid, entity)

        // Guardar localmente
        saveLocal(entity)
    }
}
