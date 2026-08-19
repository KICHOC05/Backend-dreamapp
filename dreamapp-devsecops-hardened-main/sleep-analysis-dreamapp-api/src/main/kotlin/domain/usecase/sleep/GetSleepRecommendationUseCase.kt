package team.dreamapp.com.domain.usecase.sleep

import team.dreamapp.com.infrastructure.datasouce.ollama.AiDataSource
import team.dreamapp.com.domain.repository.sleep.SleepRepository
import team.dreamapp.com.domain.model.sleep.SleepSummary

// DTO para la respuesta de recomendación
 data class SleepRecommendationResponse(
    val success: Boolean,
    val recommendation: String,
    val prompt: String
)

class GetSleepRecommendationUseCase(
    private val sleepRepository: SleepRepository,
    private val aiDataSource: AiDataSource
) {
    fun execute(uidUser: String): SleepRecommendationResponse {
        val summaries = sleepRepository.getAllSleepSummaryByUser(uidUser)
        if (summaries.isEmpty()) {
            return SleepRecommendationResponse(
                success = false,
                recommendation = "No hay registros de sueño suficientes para generar recomendaciones.",
                prompt = ""
            )
        }
        // Construir prompt con los datos de sueño
        val prompt = buildPrompt(summaries)
        val rawRecommendation = aiDataSource.generateText(prompt) ?: "No se pudo generar recomendación."
        return SleepRecommendationResponse(
            success = true,
            recommendation = rawRecommendation,
            prompt = prompt
        )
    }

    private fun buildPrompt(summaries: List<SleepSummary>): String {
        // Ejemplo simple: puedes personalizar el prompt según tu modelo
        val resumen = summaries.takeLast(7).joinToString("\n") { s ->
            "Fecha: ${s.date}, Calidad: ${s.quality}, Eficiencia: ${s.sleepEfficiency}, Duración: ${s.sleepDuration} min, Despertares: ${s.awakenings}"
        }
        return "Analiza los siguientes registros de sueño y genera recomendaciones personalizadas para mejorar el descanso:\n\n$resumen"
    }
}
