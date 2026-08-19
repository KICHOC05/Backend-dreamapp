package team.dreamapp.com.infrastructure.datasouce.firebase

import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.config.Config
import java.util.concurrent.TimeUnit

/**
 * DataSource for connecting to the Firestore Emulator in a local development environment.
 * Intended for use during development and testing, without accessing the production cloud.
 */
class FirebaseDataSourceLocal {

    private val logger = LoggerFactory.getLogger(FirebaseDataSourceLocal::class.java)

    // Internal reference to the Firestore emulator client
    private var firestore: Firestore? = null

    /**
     * Initializes the connection to the Firestore Emulator.
     * Sets required system properties and establishes the emulator service instance.
     * Throws RuntimeException if initialization fails.
     */
    fun init() {
        if (firestore != null) {
            logger.warn("Firestore has already been initialized. Ignoring duplicate call to init().")
            return
        }

        try {
            logger.info("Initializing Firestore for LOCAL environment using emulator...")

            val emulatorHost = Config.SVR_FIRESTORE_CONF.firestoreHost
            val projectId = Config.SVR_FIRESTORE_CONF.firestoreProjectId

            logger.info("Connecting to Firestore Emulator at: $emulatorHost")
            logger.info("Using project ID: $projectId")

            System.setProperty("FIRESTORE_EMULATOR_HOST", emulatorHost)
            System.setProperty("GCLOUD_PROJECT", projectId)

            val options = FirestoreOptions.newBuilder()
                .setProjectId(projectId)
                .setEmulatorHost(emulatorHost)
                .build()

            firestore = options.service

            logger.info("[OK] Connection to Firestore Emulator established successfully.")

        } catch (e: Exception) {
            logger.error("[X] Failed to initialize Firestore Emulator", e)
            throw RuntimeException("Could not initialize Firestore Emulator", e)
        }
    }

    /**
     * Returns the Firestore instance connected to the local emulator.
     * @throws IllegalStateException if `init()` has not been called beforehand.
     */
    fun getFirestoreInstance(): Firestore {
        return firestore ?: throw IllegalStateException("Firestore has not been initialized. Call init() first.")
    }

    /**
     * Performs a health check on the connection to the Firestore Emulator.
     * Executes a lightweight query to validate readiness.
     * @return true if the emulator is reachable and responsive; false otherwise
     */
    fun isConnectionHealthy(): Boolean {
        return try {
            logger.debug("Verifying connection to Firestore Emulator...")
            val result = getFirestoreInstance().collection("test").limit(1).get()
            result.get(10, TimeUnit.SECONDS)
            logger.info("[OK] Firestore Emulator connection verified successfully.")
            true
        } catch (e: Exception) {
            logger.error("[X] Firestore Emulator connection verification failed", e)
            false
        }
    }

    /**
     * Returns a description of the current environment.
     */
    fun getEnvironmentInfo(): String {
        return "Current environment: local development using Firestore Emulator"
    }
}