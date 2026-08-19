package com.example.appmobile.presentation.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmobile.data.repository.CloudSleepDataRepository
import com.example.appmobile.data.repository.SleepDataType
import com.example.appmobile.domain.model.SleepUploadResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SleepUploadViewModel(application: Application) : AndroidViewModel(application) {
    
    private val cloudRepository = CloudSleepDataRepository()
    
    // Estados de UI
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    private val _uploadResult = MutableStateFlow<UploadResult?>(null)
    val uploadResult: StateFlow<UploadResult?> = _uploadResult.asStateFlow()
    
    private val _lastUploadMessage = MutableStateFlow("")
    val lastUploadMessage: StateFlow<String> = _lastUploadMessage.asStateFlow()
    
    // Control de fechas enviadas por usuario
    private val _uploadedDates = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val uploadedDates: StateFlow<Map<String, Set<String>>> = _uploadedDates.asStateFlow()
    
    fun uploadSleepData(userId: String, dataType: SleepDataType) {
        if (_isUploading.value) {
            Log.w("SleepUploadViewModel", "Ya hay una subida en progreso")
            return
        }
        
        // Verificar fecha duplicada para todos los tipos (ahora todos usan fecha de hoy)
        val targetDate = getCurrentDate()
        
        if (isDateAlreadyUploaded(userId, targetDate)) {
            _uploadResult.value = UploadResult.Error("Ya existe una sesión de sueño para la fecha $targetDate")
            _lastUploadMessage.value = "❌ Error: Ya enviaste datos para hoy"
            return
        }
        
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _uploadResult.value = null
                _lastUploadMessage.value = "Subiendo ${dataType.displayName}..."
                
                Log.d("SleepUploadViewModel", "Iniciando subida de ${dataType.displayName} para usuario: $userId")
                
                val result = cloudRepository.uploadSampleSleepData(userId, dataType)
                
                result.fold(
                    onSuccess = { response ->
                        Log.d("SleepUploadViewModel", "Subida exitosa: ${response.message}")
                        
                        // Marcar fecha de hoy como enviada para todos los tipos
                        markDateAsUploaded(userId, targetDate)
                        
                        _uploadResult.value = UploadResult.Success(response)
                        _lastUploadMessage.value = "✅ ${dataType.displayName} subidos exitosamente"
                    },
                    onFailure = { error ->
                        Log.e("SleepUploadViewModel", "Error en subida", error)
                        _uploadResult.value = UploadResult.Error(error.message ?: "Error desconocido")
                        _lastUploadMessage.value = "❌ Error: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                Log.e("SleepUploadViewModel", "Error inesperado", e)
                _uploadResult.value = UploadResult.Error(e.message ?: "Error inesperado")
                _lastUploadMessage.value = "❌ Error inesperado: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }
    
    fun clearResult() {
        _uploadResult.value = null
        _lastUploadMessage.value = ""
    }
    
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    private fun isDateAlreadyUploaded(userId: String, date: String): Boolean {
        val userDates = _uploadedDates.value[userId] ?: emptySet()
        return userDates.contains(date)
    }
    
    private fun markDateAsUploaded(userId: String, date: String) {
        val currentDates = _uploadedDates.value.toMutableMap()
        val userDates = currentDates[userId]?.toMutableSet() ?: mutableSetOf()
        userDates.add(date)
        currentDates[userId] = userDates
        _uploadedDates.value = currentDates
    }
    
    fun canUploadForDate(userId: String, dataType: SleepDataType): Boolean {
        // Todos los tipos ahora usan la fecha de hoy
        return !isDateAlreadyUploaded(userId, getCurrentDate())
    }
}

sealed class UploadResult {
    data class Success(val response: SleepUploadResponse) : UploadResult()
    data class Error(val message: String) : UploadResult()
}
