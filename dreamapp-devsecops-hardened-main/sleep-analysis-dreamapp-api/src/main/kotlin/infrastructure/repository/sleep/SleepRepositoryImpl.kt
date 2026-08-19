package team.dreamapp.com.infrastructure.repository.sleep

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import team.dreamapp.com.domain.model.sleep.SleepSummary
import team.dreamapp.com.domain.repository.sleep.SleepRepository
import team.dreamapp.com.infrastructure.dto.sleep.SleepSummaryDto
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.URI
import java.net.http.HttpResponse
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// Get summary sleep data
class SleepRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : SleepRepository {

    private val objectMapper = jacksonObjectMapper()

    override fun getAllSleepSummaryByUser(uidUser: String): List<SleepSummary> {
        val logger = LoggerFactory.getLogger("SleepRepositoryImpl")
        val encodedUid = URLEncoder.encode(uidUser, StandardCharsets.UTF_8)
        val fullUrl = baseUrl.trimEnd('/') + "/getAllSleepSummaryByUser?uid=$encodedUid"
        logger.info("[getAllSleepSummaryByUser] Requesting from: {}", fullUrl)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(fullUrl))
            .header("X-Internal-Api-Key", System.getenv("FUNCTIONS_INTERNAL_KEY") ?: "")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        logger.info("[getAllSleepSummaryByUser] HTTP status: {}", response.statusCode())
        val sleepSummaryDto: List<SleepSummaryDto> = try {
            val rootNode = objectMapper.readTree(response.body())
            if (rootNode.has("data")) {
                objectMapper.readValue(rootNode["data"].toString())
            } else {
                emptyList()
            }
        } catch (ex: Exception) {
            logger.error("[getAllSleepSummaryByUser] Error parsing response: {}", ex.message, ex)
            emptyList()
        }

        val sleepSummary = sleepSummaryDto.map { dto ->
            SleepSummary(
                date = dto.date,
                quality = team.dreamapp.com.domain.model.sleep.Quality.fromString(dto.quality),
                sleepEfficiency = dto.sleepEfficiency,
                sleepDuration = dto.sleepDuration,
                light = dto.light,
                deep = dto.deep,
                rem = dto.rem,
                awake = dto.awake,
                avgHR = dto.avgHR,
                avgHRV = dto.avgHRV,
                awakenings = dto.awakenings
            )
        }
        logger.info("[getAllSleepSummaryByUser] Parsed {} sleep summaries", sleepSummary.size)
        return sleepSummary
    }
}
