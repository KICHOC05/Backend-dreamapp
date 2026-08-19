package com.example.appmobile.presentation.shared


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PhoneDataHolder {
    val heartRate = MutableStateFlow<Float?>(null)
    val hrv = MutableStateFlow<String?>(null)
    val sleepPhase = MutableStateFlow<String?>(null)
}