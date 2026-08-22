package team.dreamapp.com.infrastructure.repository.users

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import team.dreamapp.com.domain.entity.users.User
import team.dreamapp.com.domain.repository.users.UserRepository
import team.dreamapp.com.infrastructure.dto.user.UserDto
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

class UserRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String
) : UserRepository {

    private val objectMapper = jacksonObjectMapper()

    override fun getAllUsers(): List<User> {
        val logger = LoggerFactory.getLogger("UserRepositoryImpl")
        logger.info("[getAllUsers] Requesting users from: {}", "$baseUrl/getAllUsers")
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/getAllUsers"))
            .header("X-Internal-Api-Key", System.getenv("FUNCTIONS_INTERNAL_KEY")
                ?: error("FUNCTIONS_INTERNAL_KEY environment variable is not set"))
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        logger.info("[getAllUsers] HTTP status: {}", response.statusCode())
        val userDtos: List<UserDto> = try {
            objectMapper.readValue(response.body())
        } catch (ex: Exception) {
            logger.error("[getAllUsers] Error parsing response: {}", ex.message, ex)
            emptyList()
        }

        val users = userDtos.map { dto ->
            User(
                id = dto.uidUser,
                name = dto.username,
                weight = dto.weightKg,
                height = dto.heightCm,
                age = dto.age,
                sex = dto.sex,
                pictureUrl = dto.profilePictureUrl
            )
        }
        logger.info("[getAllUsers] Parsed {} users", users.size)
        return users
    }
}
