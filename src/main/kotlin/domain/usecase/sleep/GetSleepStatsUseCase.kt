package team.dreamapp.com.domain.usecase.sleep

import team.dreamapp.com.domain.model.sleep.SleepSummary
import team.dreamapp.com.domain.repository.sleep.SleepRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import team.dreamapp.com.infrastructure.dto.sleep.SleepEfficiencyPointDto
import team.dreamapp.com.infrastructure.dto.sleep.SleepEfficiencyChartResponse
import team.dreamapp.com.infrastructure.dto.sleep.QualityPieStatsResponse
import team.dreamapp.com.infrastructure.dto.sleep.SleepEfficiencyMonthAvgDto
import team.dreamapp.com.infrastructure.dto.sleep.SleepStatsResponse
import team.dreamapp.com.infrastructure.dto.sleep.SleepAveragesDto

class GetSleepStatsUseCase(private val sleepRepository: SleepRepository) {
    fun execute(uidUser: String): SleepStatsResponse {
        val allSummaries = sleepRepository.getAllSleepSummaryByUser(uidUser)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val oneWeekAgo = today.minusDays(6)
        val oneMonthAgo = today.minusMonths(1).plusDays(1)
        val sixMonthsAgo = today.minusMonths(6).plusDays(1)
        val oneYearAgo = today.minusYears(1).plusDays(1)

        val parsedSummaries = allSummaries.mapNotNull { summary ->
            try {
                val date = LocalDate.parse(summary.date, formatter)
                summary to date
            } catch (e: Exception) {
                null
            }
        }

        fun filterRange(start: LocalDate, end: LocalDate): List<Pair<SleepSummary, LocalDate>> =
            parsedSummaries.filter { (_, date) -> !date.isBefore(start) && !date.isAfter(end) }

        // Helper para agrupar por mes y calcular promedio
        fun toMonthAvgDto(list: List<Pair<SleepSummary, LocalDate>>): List<SleepEfficiencyMonthAvgDto> =
            list.groupBy { (_, date) -> date.year to date.monthValue }
                .map { (yearMonth, items) ->
                    val avg = items.map { (summary, _) -> summary.sleepEfficiency }.average()
                    val monthStr = "%04d-%02d".format(yearMonth.first, yearMonth.second)
                    SleepEfficiencyMonthAvgDto(date = monthStr, sleepEfficiency = avg)
                }
                .sortedBy { it.date }

        val last7Days = filterRange(oneWeekAgo, today).map { (summary, _) ->
            SleepEfficiencyPointDto(date = summary.date, sleepEfficiency = summary.sleepEfficiency)
        }.sortedBy { it.date }

        val lastMonth = filterRange(oneMonthAgo, today).map { (summary, _) ->
            SleepEfficiencyPointDto(date = summary.date, sleepEfficiency = summary.sleepEfficiency)
        }.sortedBy { it.date }

        val last6Months = toMonthAvgDto(filterRange(sixMonthsAgo, today))
        val lastYear = toMonthAvgDto(filterRange(oneYearAgo, today))

        val efficiencyChart = SleepEfficiencyChartResponse(
            last7Days = last7Days,
            lastMonth = lastMonth,
            last6Months = last6Months,
            lastYear = lastYear
        )

        // ===============================================================
        // Tabla de pastel de quality (último mes)
        // ===============================================================
        val lastMonthQuality = filterRange(oneMonthAgo, today)
            .groupingBy { (summary, _): Pair<SleepSummary, LocalDate> -> summary.quality }
            .eachCount()
        val qualityPie = QualityPieStatsResponse(lastMonth = lastMonthQuality)

        // ===============================================================
        // Averages last week
        // ===============================================================
        val last7DaysData = filterRange(oneWeekAgo, today).map { (summary, _) -> summary }
        val averages = if (last7DaysData.isNotEmpty()) {
            SleepAveragesDto(
                sleepEfficiency = last7DaysData.map { it.sleepEfficiency }.average().toInt(),
                sleepDuration = last7DaysData.map { it.sleepDuration }.average().toInt(),
                light = last7DaysData.map { it.light }.average().toInt(),
                deep = last7DaysData.map { it.deep }.average().toInt(),
                rem = last7DaysData.map { it.rem }.average().toInt(),
                awake = last7DaysData.map { it.awake }.average().toInt(),
                avgHR = last7DaysData.map { it.avgHR }.average().toInt(),
                awakenings = last7DaysData.map { it.awakenings }.sum()
            )
        } else {
            SleepAveragesDto(
                sleepEfficiency = 0,
                sleepDuration = 0,
                light = 0,
                deep = 0,
                rem = 0,
                awake = 0,
                avgHR = 0,
                awakenings = 0
            )
        }

        // ===============================================================
        // Last day stats
        // ===============================================================
        val lastDayStats = if (parsedSummaries.isNotEmpty()) {
            val mostRecentSummary = parsedSummaries.maxByOrNull { (_, date) -> date }?.first
            
            if (mostRecentSummary != null) {
                SleepAveragesDto(
                    sleepEfficiency = mostRecentSummary.sleepEfficiency.toInt(),
                    sleepDuration = mostRecentSummary.sleepDuration.toInt(),
                    light = mostRecentSummary.light.toInt(),
                    deep = mostRecentSummary.deep.toInt(),
                    rem = mostRecentSummary.rem.toInt(),
                    awake = mostRecentSummary.awake.toInt(),
                    avgHR = mostRecentSummary.avgHR.toInt(),
                    awakenings = mostRecentSummary.awakenings
                )
            } else {
                SleepAveragesDto(
                    sleepEfficiency = 0,
                    sleepDuration = 0,
                    light = 0,
                    deep = 0,
                    rem = 0,
                    awake = 0,
                    avgHR = 0,
                    awakenings = 0
                )
            }
        } else {
            SleepAveragesDto(
                sleepEfficiency = 0,
                sleepDuration = 0,
                light = 0,
                deep = 0,
                rem = 0,
                awake = 0,
                avgHR = 0,
                awakenings = 0
            )
        }

        // Return Stats
        return SleepStatsResponse(
            efficiencyChart = efficiencyChart,
            qualityPie = qualityPie,
            averagesLast7Days = averages,
            lastDayStats = lastDayStats
        )
    }
}