package team.dreamapp.com.presentation.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * DTOs para la capa de presentación
 * Estas clases pertenecen a la capa de PRESENTATION en Clean Architecture
 */

/**
 * Respuesta estándar de la API
 */
data class ApiResponse<T>(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("data") val data: T? = null,
    @JsonProperty("error") val error: String? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO para estadísticas semanales de sueño
 */
data class WeeklyStatsResponse(
    @JsonProperty("userId") val userId: String,
    @JsonProperty("weekStartDate") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") val weekStartDate: LocalDateTime,
    @JsonProperty("weekEndDate") @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") val weekEndDate: LocalDateTime,
    @JsonProperty("stats") val stats: WeeklyStatsData,
    @JsonProperty("dailyBreakdown") val dailyBreakdown: List<DailySummaryResponse>,
    @JsonProperty("summary") val summary: WeeklySummary
)

data class WeeklyStatsData(
    @JsonProperty("averageSleepHours") val averageSleepHours: Double,
    @JsonProperty("averageQuality") val averageQuality: Double,
    @JsonProperty("averageEfficiency") val averageEfficiency: Double,
    @JsonProperty("totalDeepSleep") val totalDeepSleep: Int,
    @JsonProperty("totalRemSleep") val totalRemSleep: Int,
    @JsonProperty("goodSleepDays") val goodSleepDays: Int,
    @JsonProperty("totalDays") val totalDays: Int,
    @JsonProperty("trend") val trend: String
)

data class DailySummaryResponse(
    @JsonProperty("date") @JsonFormat(pattern = "yyyy-MM-dd") val date: String,
    @JsonProperty("sleepHours") val sleepHours: Double,
    @JsonProperty("quality") val quality: Int,
    @JsonProperty("efficiency") val efficiency: Double,
    @JsonProperty("qualityLabel") val qualityLabel: String
)

data class WeeklySummary(
    @JsonProperty("overallGrade") val overallGrade: String,
    @JsonProperty("strengths") val strengths: List<String>,
    @JsonProperty("improvements") val improvements: List<String>,
    @JsonProperty("weeklyGoalProgress") val weeklyGoalProgress: Int // percentage
)

/**
 * DTO para información de salud de los servicios
 */
data class HealthCheckResponse(
    @JsonProperty("service") val service: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("details") val details: Map<String, Any>,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO para respuesta de pruebas
 */
data class TestResponse(
    @JsonProperty("testName") val testName: String,
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("result") val result: Any?,
    @JsonProperty("duration") val duration: Long? = null,
    @JsonProperty("timestamp") val timestamp: Long = System.currentTimeMillis()
)
