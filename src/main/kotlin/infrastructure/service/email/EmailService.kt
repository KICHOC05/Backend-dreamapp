package team.dreamapp.com.infrastructure.service.email

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object EmailService {
    private val objectMapper = ObjectMapper()
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build()

    fun sendVerificationCode(recipient: String, code: String) {
        val url = requiredEnv("GOOGLE_APPS_SCRIPT_URL")
        require(url.startsWith("https://script.google.com/macros/s/") && url.endsWith("/exec")) {
            "GOOGLE_APPS_SCRIPT_URL must be a deployed Google Apps Script web app URL"
        }
        val payload = objectMapper.writeValueAsString(mapOf(
            "secret" to requiredEnv("GOOGLE_APPS_SCRIPT_SECRET"),
            "recipient" to recipient,
            "code" to code
        ))
        val request = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299 && response.body().contains("\"success\":true")) {
            "Google Apps Script rejected the email request (HTTP ${response.statusCode()})"
        }
    }

    private fun requiredEnv(name: String): String = System.getenv(name)?.trim()
        ?.takeIf { it.isNotBlank() } ?: error("$name is not configured")
}
