package team.dreamapp.com.integration

import io.javalin.Javalin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import team.dreamapp.com.presentation.security.RequestSecurity
import java.net.URI
import java.net.http.HttpClient as JavaHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SecurityHeadersIntegrationTest {

    private lateinit var app: Javalin
    private lateinit var baseUrl: String
    private val httpClient = JavaHttpClient.newBuilder().build()

    @BeforeEach
    fun setUp() {
        app = Javalin.create { config ->
            config.showJavalinBanner = false
        }.start(0)
        app.before { ctx -> RequestSecurity.apply(ctx) }
        app.get("/test") { ctx -> ctx.json(mapOf("ok" to true)) }
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
    fun `response contains security headers`() {
        val response = get("/test")
        assertThat(response.statusCode()).isEqualTo(200)
        assertThat(response.headers().firstValue("X-Content-Type-Options").orElse(null)).isEqualTo("nosniff")
        assertThat(response.headers().firstValue("X-Frame-Options").orElse(null)).isEqualTo("DENY")
        assertThat(response.headers().firstValue("Referrer-Policy").orElse(null)).isEqualTo("no-referrer")
        assertThat(response.headers().firstValue("Cache-Control").orElse(null)).isEqualTo("no-store")
    }

    @Test
    fun `response content type is json`() {
        val response = get("/test")
        assertThat(response.headers().firstValue("Content-Type").orElse(null)).contains("application/json")
    }
}
