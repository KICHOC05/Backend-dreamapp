package team.dreamapp.com.infrastructure.datasouce.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.config.Config
import java.util.concurrent.TimeUnit

/**
 * DataSource for connecting to Google Firestore in a production environment.
 * This class handles authentication and provides access to the Firestore client instance.
 */
class FirebaseDataSourceProduction {

    private val logger = LoggerFactory.getLogger(FirebaseDataSourceProduction::class.java)

    // Internal reference to the Firestore client
    private var firestore: Firestore? = null

    /**
     * Initializes the Firebase application and connects to the Firestore database.
     * Loads credentials from the `serviceAccountKey.json` file located in the resources directory.
     * Throws RuntimeException if initialization fails.
     */
    fun init() {
        try {
            logger.info("Starting Firestore PRODUCTION configuration...")

            val serviceAccountStream = this::class.java.classLoader
                .getResourceAsStream("serviceAccountKey.json")
                ?: throw IllegalStateException("Resource 'serviceAccountKey.json' not found")

            val credentials = GoogleCredentials.fromStream(serviceAccountStream)
            val projectId = Config.SVR_FIRESTORE_CONF.firestoreProjectId

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("[OK] FirebaseApp initialized successfully")
            }

            firestore = FirestoreOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .service

            logger.info("[OK] Firestore PRODUCTION connection established")

        } catch (e: Exception) {
            logger.error("[X] Failed to initialize Firestore PRODUCTION", e)
            throw RuntimeException("Could not initialize Firestore PRODUCTION", e)
        }
    }

    /**
     * Performs a health check on the Firestore connection.
     * Executes a lightweight query to verify communication and readiness.
     * @return true if the connection is valid and responsive; false otherwise
     */
    fun isConnectionHealthy(): Boolean {
        return try {
            val db = firestore ?: throw IllegalStateException("Firestore is not initialized")
            db.collection("test").limit(1).get().get(10, TimeUnit.SECONDS)
            logger.info("[OK] Firestore health check successful")
            true
        } catch (e: Exception) {
            logger.error("[X] Firestore health check failed", e)
            false
        }
    }

    /**
     * Provides access to the initialized Firestore client.
     * @return the active Firestore instance
     * @throws IllegalStateException if Firestore has not been initialized
     */
    fun getFirestoreInstance(): Firestore {
        return firestore ?: throw IllegalStateException("Firestore is not initialized. Call init() first.")
    }

    /**
     * Returns a brief description of the current Firestore environment.
     */
    fun getEnvironmentInfo(): String = "Production - Firestore Cloud"
}