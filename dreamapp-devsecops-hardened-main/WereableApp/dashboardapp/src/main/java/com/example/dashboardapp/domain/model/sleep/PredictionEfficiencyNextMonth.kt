package com.example.dashboardapp.domain.model.sleep


data class PredictionEfficiencyNextMonth(
    val efficiencyPredictions: List<PredictionEfficiencyNextMonthPoint>
)

data class PredictionEfficiencyNextMonthPoint(
    val date: String,
    val efficiency: Double
)