package com.example.dashboardapp.data.remote.dto.sleep

// Response dto

data class PredictionEfficiencyNextMonthResponseDto(
    val success: Boolean,
    val nextMonthPredictions: List<PredictionEfficiencyNextMonthPointDto>?
)

// Point dto

data class PredictionEfficiencyNextMonthPointDto(
    val date: String,
    val sleepEfficiency: Double
)