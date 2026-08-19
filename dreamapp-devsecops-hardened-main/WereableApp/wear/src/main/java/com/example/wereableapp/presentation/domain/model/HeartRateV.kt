package com.example.wereableapp.presentation.domain.model

data class HeartRateV(
    val rrIntervals: List<Long>,
    val rmssd: Double,
    val sdnn: Double,
    val timestamp: Long
)