package com.example.dashboardapp.data.remote.dto.sleep

import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonth
import com.example.dashboardapp.domain.model.sleep.PredictionEfficiencyNextMonthPoint

fun PredictionEfficiencyNextMonthResponseDto.toDomain(): PredictionEfficiencyNextMonth? {
    return nextMonthPredictions?.let { predictions ->
        PredictionEfficiencyNextMonth(
            efficiencyPredictions = predictions.map { it.toDomain() }
        )
    }
}

fun PredictionEfficiencyNextMonthPointDto.toDomain(): PredictionEfficiencyNextMonthPoint {
    return PredictionEfficiencyNextMonthPoint(
        date = date,
        efficiency = sleepEfficiency
    )
}