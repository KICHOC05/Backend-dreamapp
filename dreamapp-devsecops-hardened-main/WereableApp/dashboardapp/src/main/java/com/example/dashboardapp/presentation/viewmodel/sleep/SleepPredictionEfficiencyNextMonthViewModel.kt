package com.example.dashboardapp.presentation.viewmodel.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonth
import com.example.dashboardapp.domain.usecase.sleep.GetPredictEfficiencyNextMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SleepPredictionEfficiencyNextMonthUiState {
    object Loading : SleepPredictionEfficiencyNextMonthUiState()
    data class Success(val data: PredictionEfficiencyNextMonth) : SleepPredictionEfficiencyNextMonthUiState()
    data class Error(val message: String) : SleepPredictionEfficiencyNextMonthUiState()
}

@HiltViewModel
class SleepPredictionEfficiencyNextMonthViewModel @Inject constructor(
    private val getPredictEfficiencyNextMonthUseCase: GetPredictEfficiencyNextMonthUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<SleepPredictionEfficiencyNextMonthUiState>(
        SleepPredictionEfficiencyNextMonthUiState.Loading)
    val uiState: StateFlow<SleepPredictionEfficiencyNextMonthUiState> = _uiState

    fun loadSleepPredictionEfficiencyNextMonth(uid: String) {
        _uiState.value = SleepPredictionEfficiencyNextMonthUiState.Loading
        viewModelScope.launch {
            val result = getPredictEfficiencyNextMonthUseCase(uid)
            if (result.isSuccess) {
                val predictionEfficiencyNextMonth = result.getOrNull()
                if (predictionEfficiencyNextMonth != null) {
                    _uiState.value = SleepPredictionEfficiencyNextMonthUiState.Success(
                        predictionEfficiencyNextMonth)
                } else {
                    _uiState.value = SleepPredictionEfficiencyNextMonthUiState.Error("Datos vacíos recibidos")
                }
            } else {
                _uiState.value = SleepPredictionEfficiencyNextMonthUiState.Error(result.exceptionOrNull()?.localizedMessage ?: "Error desconocido")
            }
        }
    }
}