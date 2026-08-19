package com.example.appmobile.data.repository;

//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object HeartRateRepository {
    private val _bpm = MutableStateFlow(0f)
    val bpm: StateFlow<Float> = _bpm

    private val _bpmHistory = MutableStateFlow<List<Float>>(emptyList())
    val bpmHistory: StateFlow<List<Float>> = _bpmHistory

    fun updateBPM(newBPM: Float) {
        _bpm.value = newBPM
        _bpmHistory.value = (_bpmHistory.value + newBPM).takeLast(30) // Limita a últimos 30 valores
    }

    fun clearHistory() {
        _bpmHistory.value = emptyList()
    }
}
//object HeartRateRepository {
//    private val _bpm = mutableStateOf(0f)
//    val bpm: State<Float> get() = _bpm
//
//    fun updateBPM(value: Float) {
//        Log.d("HeartRateRepository", "BPM actualizado: $value")
//        _bpm.value = value
//    }
//}