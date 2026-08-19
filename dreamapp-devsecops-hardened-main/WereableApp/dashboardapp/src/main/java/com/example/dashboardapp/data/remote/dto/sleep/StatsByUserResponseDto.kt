package com.example.dashboardapp.data.remote.dto.sleep

import com.google.gson.annotations.SerializedName

// Response dto
data class StatsByUserResponseDto(
    val success: Boolean,
    val data: Chars
)

// Charts
data class Chars(
    val efficiencyChart: EfficiencyChart,
    val qualityPie: QualityChartWrapper,
    val lastDayStats: StatsAndAverages,
    val averagesLast7Days: StatsAndAverages
)

// =================================
// Chart efficiency
// =================================
data class SleepEfficiencyPointDto(
    val date: String,
    val sleepEfficiency: Double
)

data class EfficiencyChart(
    val last7Days: List<SleepEfficiencyPointDto>,
    val lastMonth: List<SleepEfficiencyPointDto>,
    val last6Months: List<SleepEfficiencyPointDto>,
    val lastYear: List<SleepEfficiencyPointDto>
)

// =================================
// Averages last
// =================================

data class StatsAndAverages(
    val sleepEfficiency: Int,
    val sleepDuration: Int,
    val light: Int,
    val deep: Int,
    val rem: Int,
    val awake: Int,
    val avgHR: Int,
    val awakenings: Int
)

// =================================
// Quality chart
// =================================

data class QualityChartWrapper(
    val lastMonth: Quality
)

data class Quality(
    @SerializedName("GOOD") val good: Int,
    @SerializedName("FAIR") val fair: Int,
    @SerializedName("POOR") val poor: Int,
    @SerializedName("EXCELLENT") val excellent: Int
)