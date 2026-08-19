package com.example.dashboardapp.data.remote.dto.sleep

import com.example.dashboardapp.domain.model.sleep.EfficiencyStats
import com.example.dashboardapp.domain.model.sleep.QualityStats
import com.example.dashboardapp.domain.model.sleep.SleepEfficiencyPoint
import com.example.dashboardapp.domain.model.sleep.SleepStats
import com.example.dashboardapp.domain.model.sleep.StatsAndAveragesDomain
import kotlin.Int

fun StatsByUserResponseDto.toDomain(): SleepStats {
    return data.toDomain() // Solo delegas el mapeo al `data`
}

fun Chars.toDomain(): SleepStats {
    return SleepStats(
        efficiency = efficiencyChart.toDomain(),
        quality = qualityPie.toDomain(),
        statsLastDay = lastDayStats.toDomain(),
        averagesLastWeek = averagesLast7Days.toDomain()
    )
}

fun EfficiencyChart.toDomain(): EfficiencyStats {
    return EfficiencyStats(
        last7Days = last7Days.map { it.toDomain() },
        lastMonth = lastMonth.map { it.toDomain() },
        last6Months = last6Months.map { it.toDomain() },
        lastYear = lastYear.map { it.toDomain() }
    )
}

fun SleepEfficiencyPointDto.toDomain(): SleepEfficiencyPoint {
    return SleepEfficiencyPoint(
        date = date,
        efficiency = sleepEfficiency
    )
}

fun StatsAndAverages.toDomain(): StatsAndAveragesDomain {
    return StatsAndAveragesDomain(
        sleepEfficiency = sleepEfficiency,
        sleepDuration = sleepDuration,
        light = light,
        deep = deep,
        rem = rem,
        awake = awake,
        avgHR = avgHR,
        awakenings = awakenings
    )
}

fun QualityChartWrapper.toDomain(): QualityStats {
    return QualityStats(
        good = lastMonth.good,
        fair = lastMonth.fair,
        poor = lastMonth.poor,
        excellent = lastMonth.excellent
    )
}