package com.example.dashboardapp.presentation.viewmodel.database

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashboardapp.domain.usecase.database.ExportDatabaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DatabaseExportViewModel @Inject constructor(
    private val exportDatabaseUseCase: ExportDatabaseUseCase
) : ViewModel() {

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState

    fun exportDatabase(context: Context) {
        viewModelScope.launch {
            _exportState.value = ExportState.Loading
            
            exportDatabaseUseCase.execute(context)
                .onSuccess { filePath ->
                    _exportState.value = ExportState.Success(filePath)
                }
                .onFailure { error ->
                    _exportState.value = ExportState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun resetState() {
        _exportState.value = ExportState.Idle
    }
}

sealed class ExportState {
    object Idle : ExportState()
    object Loading : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}
