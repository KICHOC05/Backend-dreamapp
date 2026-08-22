package team.dreamapp.com.infrastructure.config

import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class ServerAuthConfig(
    val jdbcProtocol: String,
    val dbHost: String,
    val dbPort: String,
    val dbName: String,
    val dbUser: String,
    val dbPwd: String,
    val dbEncoding: String
) {
    val dbURL get() = "$jdbcProtocol://$dbHost:$dbPort/$dbName?encoding=$dbEncoding"
}

class ServerAiConfig(
    val baseUrl: String,
    val model: String,
    val apiKey: String
) {
    val chatCompletionsUrl get() = baseUrl.trimEnd('/') + "/chat/completions"
}

class ServerFirestoreConfig(
    val projectId: String,
    val host: String,
    val functionsUrl: String
) {
    val firestoreProjectId get() = projectId
    val firestoreHost get() = host
    val firestoreFunctionsURL get() = functionsUrl
}

object Config {

    private val log = LoggerFactory.getLogger(this::class.java)
    val SVR_AUTH_CONF: ServerAuthConfig
    val SVR_AI_CONF: ServerAiConfig
    val SVR_FIRESTORE_CONF: ServerFirestoreConfig


    init {
        val prop = Properties().apply {
            val file = File(System.getenv("CONFIG_FILE") ?: "config/server.properties")
            if (file.exists()) file.inputStream().use { stream -> load(stream) }
        }
        fun value(env: String, property: String, default: String = ""): String =
            System.getenv(env)?.takeIf { it.isNotBlank() }
                ?: prop.getProperty(property)?.takeIf { it.isNotBlank() }
                ?: default
        /* */
        SVR_AUTH_CONF = ServerAuthConfig(
            jdbcProtocol = value("DB_JDBC_PROTOCOL", "database.jdbc.protocol", "jdbc:firebird"),
            dbHost = value("DB_HOST", "database.host", "localhost"),
            dbPort = value("DB_PORT", "database.port", "3051"),
            dbName = value("DB_NAME", "database.name", "db_dashboard"),
            dbUser = value("DB_USER", "database.user", "sysdba"),
            dbPwd = value("DB_PASSWORD", "database.password"),
            dbEncoding = value("DB_ENCODING", "database.encoding", "UTF8")
        )
        SVR_AI_CONF = ServerAiConfig(
            baseUrl = value("AI_BASE_URL", "ai.base-url", "https://api.groq.com/openai/v1"),
            model = value("AI_MODEL", "ai.model", "openai/gpt-oss-20b"),
            apiKey = value("AI_API_KEY", "ai.api-key")
        )
        SVR_FIRESTORE_CONF = ServerFirestoreConfig(
            projectId = value("FIREBASE_PROJECT_ID", "firebase.project-id"),
            host = value("FIREBASE_HOST", "firebase.host", "localhost:8080"),
            functionsUrl = value("FIREBASE_FUNCTIONS_URL", "firebase.url-functions")
        )
    }
}
