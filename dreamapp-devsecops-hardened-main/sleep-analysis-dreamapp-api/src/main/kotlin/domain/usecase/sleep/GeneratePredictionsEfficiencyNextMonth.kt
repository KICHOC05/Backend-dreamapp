package team.dreamapp.com.domain.usecase.sleep

import team.dreamapp.com.domain.repository.sleep.SleepRepository
import team.dreamapp.com.infrastructure.datasouce.ollama.AiDataSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.slf4j.LoggerFactory

data class SleepEfficiencyPredictionDto(
    val date: String,
    val sleepEfficiency: Double
)

class GeneratePredictionsEfficiencyNextMonth(
    private val sleepRepository: SleepRepository,
    private val aiDataSource: AiDataSource
) {
    private val logger = LoggerFactory.getLogger(GeneratePredictionsEfficiencyNextMonth::class.java)

    fun execute(uidUser: String): List<SleepEfficiencyPredictionDto> {
        
        val summaries = sleepRepository.getAllSleepSummaryByUser(uidUser)
        
        if (summaries.isEmpty()) {
            val result = generateFallbackPredictions()
            return result
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        
        // Generate exactly 30 days starting from tomorrow (fixed to ensure exactly 30 days)
        val next30Days = (1..30).map { day ->
            today.plusDays(day.toLong()).format(formatter)
        }

        // Build realistic prompt for AI - using ALL available data
        val recentData = summaries.joinToString(",") { "${it.sleepEfficiency}" }
        val avgEfficiency = String.format("%.1f", summaries.map { it.sleepEfficiency }.average())

        // Prompt
        val prompt = """
            Based on this person's sleep efficiency history (average: ${avgEfficiency}%, data: [${recentData}]), 
            predict 30 realistic sleep efficiency values for the next 30 days.
            
            Requirements:
            - Values should be realistic percentages between 60-95%
            - Include natural daily variations
            - Consider weekday/weekend patterns
            - Maintain consistency with historical average
            - No extreme values or perfect patterns
            
            Return ONLY comma-separated numbers (no text): 78.5,82.3,79.1,85.7...
        """.trimIndent()
        
        // Try AI prediction with moderate temperature for realistic variability
        val rawResponse = try {
            aiDataSource.generateText(prompt, temperature = 0.4)
        } catch (e: Exception) {
            logger.warn("[PredictionUseCase] AI request failed: ${e.message}, using fallback")
            null
        }
        
        if (rawResponse == null || rawResponse.isBlank()) {
            val fallbackResult = generateFallbackPredictions()
            val endTime = System.currentTimeMillis()
            return fallbackResult
        }
        
        // Parse AI response - let AI generate realistic values without modifications
        return try {
            // Find the line with comma-separated numbers
            val lines = rawResponse.split('\n')
            val numberLine = lines.find { line ->
                line.contains(',') && line.matches(Regex(".*\\d+(?:\\.\\d+)?(?:,\\d+(?:\\.\\d+)?)+.*"))
            } ?: rawResponse
            
            logger.info("[PredictionUseCase] 🔍 Selected line for parsing: '$numberLine'")
            
            // Extract the comma-separated numbers
            val numbersRegex = Regex("\\d+(?:\\.\\d+)?(?:,\\d+(?:\\.\\d+)?)+")
            val match = numbersRegex.find(numberLine)
            
            val efficiencyValues = if (match != null) {
                // Parse numbers directly from AI response - only ensure 0-100 range
                match.value.split(',')
                    .map { it.trim().toDouble() }
                    .map { it.coerceIn(0.0, 100.0) } // Only ensure valid percentage range
                    .take(30)
                    .toList()
            } else {
                emptyList()
            }
            
            if (efficiencyValues.size >= 25) {
                
                // Use AI values directly - no additional modifications
                val finalPredictions = next30Days.take(efficiencyValues.size).zip(efficiencyValues) { date, efficiency ->
                    SleepEfficiencyPredictionDto(date, efficiency)
                }
                
                // If we have fewer than 30, pad with fallback for missing days only
                val paddedPredictions = if (finalPredictions.size < 30) {
                    val remaining = next30Days.drop(finalPredictions.size)
                    val fallbackForRemaining = remaining.map { date ->
                        val baseEff = avgEfficiency.toDouble()
                        val variance = (-8..8).random().toDouble()
                        SleepEfficiencyPredictionDto(date, (baseEff + variance).coerceIn(0.0, 100.0))
                    }
                    finalPredictions + fallbackForRemaining
                } else {
                    finalPredictions
                }
                
                paddedPredictions
            } else {
                val fallbackResult = generateFallbackPredictions()
                fallbackResult
            }
        } catch (e: Exception) {
            val fallbackResult = generateFallbackPredictions()
            fallbackResult
        }
    }
    
    private fun generateFallbackPredictions(): List<SleepEfficiencyPredictionDto> {
        logger.info("[PredictionUseCase] Generating realistic fallback predictions (30 days)")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val baseEfficiency = 82.0 // More realistic base
        
        val fallbackPredictions = (1..30).map { day ->
            val date = today.plusDays(day.toLong()).format(formatter)
            // More realistic variance - ensure 0-100 range
            val variance = (-8..8).random().toDouble()
            val efficiency = (baseEfficiency + variance).coerceIn(0.0, 100.0) // Only ensure valid percentage range
            SleepEfficiencyPredictionDto(date, efficiency)
        }
        
        return fallbackPredictions
    }
}
