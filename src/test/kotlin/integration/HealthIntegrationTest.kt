package team.dreamapp.com.integration

import io.javalin.Javalin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import java.net.http.HttpClient as JavaHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HealthIntegrationTest {

    private lateinit var app: Javalin
    private lateinit var baseUrl: String
    private val httpClient = JavaHttpClient.newBuilder().build()

    @BeforeEach
    fun setUp() {
        app = Javalin.create { config ->
            config.startup.showJavalinBanner = false
            config.routes.get("/health") { ctx -> ctx.json(mapOf("status" to "ok")) }
            config.routes.get("/") { ctx -> ctx.json(mapOf("message" to "Server Javalin")) }
        }.start(0)
        baseUrl = "http://localhost:${app.port()}"
    }

    @AfterEach
    fun tearDown() {
        app.stop()
    }

    private fun get(path: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl$path"))
            .GET()
            .build()
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

    @Test
    fun `health endpoint returns ok`() {
        val response = get("/health")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).contains("ok")
    }

    @Test
    fun `root endpoint returns server message`() {
        val response = get("/")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.body()).contains("Server Javalin")
    }

    @Test
    fun `unknown endpoint returns 404`() {
        val response = get("/nonexistent")
        assertThat(response.statusCode()).isEqualTo(404)
    }
}
