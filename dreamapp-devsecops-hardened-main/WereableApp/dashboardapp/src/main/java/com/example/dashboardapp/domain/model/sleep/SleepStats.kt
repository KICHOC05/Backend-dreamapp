package com.example.dashboardapp.domain.model.sleep

data class SleepStats(
    val efficiency: EfficiencyStats,
    val quality: QualityStats,
    val statsLastDay: StatsAndAveragesDomain,
    val averagesLastWeek: StatsAndAveragesDomain
)

data class EfficiencyStats(
    val last7Days: List<SleepEfficiencyPoint>,
    val lastMonth: List<SleepEfficiencyPoint>,
    val last6Months: List<SleepEfficiencyPoint>,
    val lastYear: List<SleepEfficiencyPoint>
)

data class SleepEfficiencyPoint(
    val date: String,
    val efficiency: Double
)

data class QualityStats(
    val good: Int,
    val fair: Int,
    val poor: Int,
    val excellent: Int
)

data class StatsAndAveragesDomain(
    val sleepEfficiency: Int,
    val sleepDuration: Int,
    val light: Int,
    val deep: Int,
    val rem: Int,
    val awake: Int,
    val avgHR: Int,
    val awakenings: Int
)