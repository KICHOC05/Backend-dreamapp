package com.example.dashboardapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.data.remote.helpers.NotifyUpdateHelper
import com.example.dashboardapp.data.remote.dto.users.UserDto
import com.example.dashboardapp.data.remote.dto.users.toDomain
import com.example.dashboardapp.domain.model.user.User
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.gson.Gson
import javax.inject.Named

@HiltViewModel
class UserListViewModel @Inject constructor(
    @Named("webSocketUrl") private val webSocketUrl: String,
    private val notifyUpdateHelper: NotifyUpdateHelper
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private val gson = Gson()

    fun connectWebSocket() {
        _isLoading.value = true
        val client = OkHttpClient()
        val request = Request.Builder().url(webSocketUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                viewModelScope.launch { _isLoading.value = false }
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch {
                    try {
                        val usersDto = gson.fromJson(text, Array<UserDto>::class.java).toList()
                        val users = usersDto.map { it.toDomain() }
                        _users.value = users
                        _isLoading.value = false
                    } catch (e: Exception) {
                        _error.value = "Error to read users: ${e.message}"
                        _isLoading.value = false
                    }
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                viewModelScope.launch {
                    _error.value = "WebSocket error: ${t.message}"
                    _isLoading.value = false
                }
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModelScope.launch { _isLoading.value = false }
            }
        })
    }

    suspend fun notifyUpdate() {
        notifyUpdateHelper.notifyUpdate()
    }

    fun disconnectWebSocket() {
        webSocket?.close(1000, "Screen closed")
        webSocket = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
