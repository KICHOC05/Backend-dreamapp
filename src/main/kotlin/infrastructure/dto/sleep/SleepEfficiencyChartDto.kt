package team.dreamapp.com.infrastructure.dto.sleep

data class SleepEfficiencyPointDto(
    val date: String,
    val sleepEfficiency: Double
)

data class SleepEfficiencyMonthAvgDto(
    val date: String, // yyyy-MM
    val sleepEfficiency: Double
)

data class SleepEfficiencyChartResponse(
    val last7Days: List<SleepEfficiencyPointDto> = emptyList(),
    val lastMonth: List<SleepEfficiencyPointDto> = emptyList(),
    val last6Months: List<SleepEfficiencyMonthAvgDto> = emptyList(),
    val lastYear: List<SleepEfficiencyMonthAvgDto> = emptyList(),
)
