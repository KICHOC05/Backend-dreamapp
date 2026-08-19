package team.dreamapp.com.infrastructure.dto.sleep

data class SleepSummaryDto(
    val date: String,
    val quality: String,
    val sleepEfficiency: Double,
    val sleepDuration: Int,
    val light: Int,
    val deep: Int,
    val rem: Int,
    val awake: Int,
    val avgHR: Int,
    val avgHRV: Int,
    val awakenings: Int
)