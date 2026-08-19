package team.dreamapp.com.infrastructure.datasouce.ollama

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.config.Config
import java.util.concurrent.TimeUnit

/**
 * DataSource responsible for managing the connection and interactions with the Ollama AI server.
 */
class AiDataSource {

    private val logger = LoggerFactory.getLogger(AiDataSource::class.java)

    // HTTP client with defined timeouts for reliable communication
    private val client = OkHttpClient.Builder()
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(1000, TimeUnit.SECONDS)
        .writeTimeout(1000, TimeUnit.SECONDS)
        .build()

    // JSON mapper for parsing and serializing request/response payloads
    private val objectMapper = ObjectMapper().apply {
        registerKotlinModule()
    }

    // Server and model configuration values loaded from external configuration
    private val config = Config.SVR_AI_CONF
    private val modelName = config.model

    private var initialized = false

    /**
     * Initializes the DataSource by validating both server and model availability.
     * Throws an exception if the server is not reachable.
     */
    fun init() {
        logger.info("Initializing external AI provider...")

        if (!isServerAvailable()) {
            throw IllegalStateException("AI provider is unavailable or AI_API_KEY is missing")
        }

        initialized = true
        logger.info("[OK] External AI datasource initialized successfully")
    }

    /**
     * Checks if the Ollama server is up and responding to basic requests.
     * @return true if available; false otherwise
     */
    fun isServerAvailable(): Boolean {
        return try {
            if (config.apiKey.isBlank()) return false
            val request = Request.Builder()
                .url(config.baseUrl.trimEnd('/') + "/models")
                .header("Authorization", "Bearer ${config.apiKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val available = response.isSuccessful
                if (available) {
                    logger.info("[OK] AI provider is reachable")
                } else {
                    logger.warn("[X] Ollama server responded with status: ${response.code}")
                }
                available
            }
        } catch (e: Exception) {
            logger.error("[X] Error connecting to Ollama server: ${e.message}")
            false
        }
    }

    /**
     * Verifies whether the configured AI model is available on the Ollama server.
     * @return true if the model is listed among available models; false otherwise
     */
    fun isModelAvailable(): Boolean {
        return isServerAvailable()
    }

    /**
     * Sends a prompt to the AI model and returns the generated response.
     * @param prompt Text prompt to be processed by the AI
     * @param temperature Controls randomness in output (default: 0.7)
     * @param topP Controls nucleus sampling (default: 0.9)
     * @param maxTokens Maximum number of tokens to generate (default: 1000)
     * @return Generated text or null if an error occurs
     * @throws IllegalStateException if called before initialization
     */
    fun generateText(
        prompt: String,
        temperature: Double = 0.7,
        topP: Double = 0.9,
        maxTokens: Int = 1000
    ): String? {
        if (!initialized) throw IllegalStateException("Datasource not initialized")

        return try {
            val requestBody = mapOf(
                "model" to modelName,
                "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
                "stream" to false,
                "temperature" to temperature,
                "top_p" to topP,
                "max_completion_tokens" to maxTokens
            )

            val json = objectMapper.writeValueAsString(requestBody)
            val body = json.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(config.chatCompletionsUrl)
                .header("Authorization", "Bearer ${config.apiKey}")
                .post(body)
                .build()

            logger.info("Sending prompt to Ollama server: ${prompt.take(100)}...")

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("[X] Ollama request failed with status: ${response.code}")
                    return null
                }

                val responseBody = response.body?.string() ?: return null
                val root = objectMapper.readTree(responseBody)
                val generatedText = root.path("choices").path(0).path("message").path("content").asText(null)

                logger.info("[OK] Received response from Ollama (${generatedText?.length ?: 0} characters)")
                return generatedText
            }
        } catch (e: Exception) {
            logger.error("[X] Error during text generation", e)
            null
        }
    }

    /**
     * Retrieves metadata from the Ollama server, including available models and server info.
     * @return Map with structured server and model information or error metadata
     */
    fun getServerInfo(): Map<String, Any> {
        return try {
            mapOf("providerUrl" to config.baseUrl, "selectedModel" to modelName,
                "status" to if (isServerAvailable()) "available" else "unavailable")
        } catch (e: Exception) {
            mapOf(
                "error" to (e.message ?: "Unknown error"),
                "status" to "error",
                "providerUrl" to config.baseUrl
            )
        }
    }

    /**
     * Performs a basic test request to ensure that the model responds to a simple prompt.
     * @return Map with results including availability, prompt, and response
     */
    fun testConnection(): Map<String, Any> {
        val testPrompt = "Hello, this is a connection test. Please respond with 'Connection successful!'"
        val response = generateText(testPrompt)

        return mapOf(
            "serverAvailable" to isServerAvailable(),
            "modelAvailable" to isModelAvailable(),
            "testPrompt" to testPrompt,
            "testResponse" to (response ?: "No response"),
            "connectionWorking" to (response != null)
        )
    }

    /**
     * Envía un prompt y recibe la respuesta en streaming, llamando a onChunk por cada fragmento recibido.
     * @param prompt Texto del prompt
     * @param stream Si es true, activa el modo streaming en Ollama
     * @param onChunk Callback que recibe (chunk, finished)
     */
    fun generateTextStream(
        prompt: String,
        stream: Boolean = true,
        temperature: Double = 0.7,
        topP: Double = 0.9,
        maxTokens: Int = 1000,
        onChunk: (chunk: String?, finished: Boolean) -> Unit
    ) {
        if (!initialized) throw IllegalStateException("Datasource not initialized")

        onChunk(generateText(prompt, temperature, topP, maxTokens), true)
    }

    /**
     * Returns true if both the server and the configured model are reachable and functioning.
     */
    fun isConnectionHealthy(): Boolean {
        return isServerAvailable() && isModelAvailable()
    }
}
