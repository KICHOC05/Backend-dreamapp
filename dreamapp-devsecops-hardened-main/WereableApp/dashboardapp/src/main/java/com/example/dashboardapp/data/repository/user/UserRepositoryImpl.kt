package com.example.dashboardapp.data.repository.user

import com.example.dashboardapp.data.remote.api.user.UserApiService
import com.example.dashboardapp.data.remote.dto.users.toDomain
import com.example.dashboardapp.data.remote.dto.users.UserDto
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.domain.repository.user.UserRepository
import javax.inject.Inject

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class UserRepositoryImpl @Inject constructor(
    private val api: UserApiService
) : UserRepository {
    override suspend fun getAllUsers(): Result<List<User>> {
        try {
            val response = api.getAllUsers()
            if (response.isSuccessful) {
                val usersDto = response.body() ?: emptyList()
                return Result.success(usersDto.map { it.toDomain() })
            } else {
                return Result.failure(Exception("Error to get users: HTTP ${'$'}{response.code()}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun observeUsersWebSocket(url: String): Flow<List<User>> = callbackFlow {
        val gson = Gson()
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val usersDto = gson.fromJson(text, Array<UserDto>::class.java).toList()
                    val users = usersDto.map { it.toDomain() }
                    trySend(users)
                } catch (_: Exception) {}
            }
        })
        awaitClose { webSocket.close(1000, "Screen closed") }
    }
}