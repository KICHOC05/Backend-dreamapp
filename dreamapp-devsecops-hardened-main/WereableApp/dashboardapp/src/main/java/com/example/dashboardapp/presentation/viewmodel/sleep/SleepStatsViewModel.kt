
package com.example.dashboardapp.presentation.viewmodel.sleep

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.domain.model.sleep.SleepStats
import com.example.dashboardapp.domain.usecase.sleep.GetSleepStatsByUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


sealed class SleepStatsUiState {
    object Loading : SleepStatsUiState()
    data class Success(val data: SleepStats) : SleepStatsUiState()
    data class Error(val message: String) : SleepStatsUiState()
}

@HiltViewModel
class SleepStatsViewModel @Inject constructor(
    private val getSleepStatsByUserUseCase: GetSleepStatsByUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SleepStatsUiState>(SleepStatsUiState.Loading)
    val uiState: StateFlow<SleepStatsUiState> = _uiState

    fun loadSleepStats(uid: String) {
        _uiState.value = SleepStatsUiState.Loading
        viewModelScope.launch {
            val result = getSleepStatsByUserUseCase(uid)
            if (result.isSuccess) {
                val sleepStats = result.getOrNull()
                if (sleepStats != null) {
                    _uiState.value = SleepStatsUiState.Success(sleepStats)
                } else {
                    _uiState.value = SleepStatsUiState.Error("Datos vacíos recibidos")
                }
            } else {
                _uiState.value = SleepStatsUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Error desconocido")
            }
        }
    }
}