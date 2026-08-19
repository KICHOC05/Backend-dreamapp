package team.dreamapp.com.infrastructure.dto.sleep

data class SleepAveragesDto(
    val sleepEfficiency: Int,
    val sleepDuration: Int,
    val light: Int,
    val deep: Int,
    val rem: Int,
    val awake: Int,
    val avgHR: Int,
    val awakenings: Int
)