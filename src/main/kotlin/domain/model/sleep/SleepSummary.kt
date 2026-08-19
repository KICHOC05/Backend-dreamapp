package team.dreamapp.com.domain.model.sleep

data class SleepSummary(
    val date: String,
    val quality: Quality,
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