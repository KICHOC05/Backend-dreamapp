package team.dreamapp.com.infrastructure.dto.sleep

import team.dreamapp.com.domain.model.sleep.Quality

data class QualityPieStatsResponse(
    val lastMonth: Map<Quality, Int> = emptyMap()
)

data class SleepStatsResponse(
    val efficiencyChart: SleepEfficiencyChartResponse,
    val qualityPie: QualityPieStatsResponse,
    val averagesLast7Days: SleepAveragesDto,
    val lastDayStats: SleepAveragesDto
)