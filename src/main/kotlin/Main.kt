package team.dreamapp.com

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.delete
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.patch
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.validation.ValidationException
import org.slf4j.LoggerFactory
import team.dreamapp.com.domain.entity.auth.Role
import team.dreamapp.com.infrastructure.datasouce.authdatabase.AuthDataSource
import team.dreamapp.com.presentation.auth.AccessManager
import team.dreamapp.com.presentation.controller.account.UserAccountController
import team.dreamapp.com.presentation.controller.auth.AuthController
import team.dreamapp.com.presentation.controller.auth.RegistrationController
import team.dreamapp.com.presentation.controller.sleep.SleepAiController
import team.dreamapp.com.presentation.controller.sleep.SleepStatsController
import team.dreamapp.com.presentation.controller.sleep.SleepStateController
import team.dreamapp.com.presentation.controller.users.UserController
import team.dreamapp.com.presentation.controller.subscription.SubscriptionController
import team.dreamapp.com.presentation.security.RequestSecurity
import team.dreamapp.com.presentation.security.WebOriginPolicy

/**
 * Entry point of the DreamApp backend server.
 *
 * This Kotlin application uses Javalin as its web framework and initializes
 * three main data sources required for the system to function properly:
 *
 * 1. **Authentication Database**: Handles user authentication and credential validation.
 * 2. **Firestore (Firebase)**: Acts as the main document-based database for user data.
 * 3. **Ollama Server**: External AI model server used for processing or inference.
 *
 *  The application performs the following steps on startup:
 * - Validates the connection to each datasource using a generic validation utility.
 * - Initializes the Javalin HTTP server on port 7070.
 * - Configures middleware to enforce JSON response content-type.
 * - Defines a basic root endpoint (`/`) for server availability checks.
 *
 *  Defined Endpoints
 * - Authentication Endpoints (`/auth`)
 * - Account Management Endpoints (`/account`)
 *
 * - Fot get info users (Firebase) (`/users`)
 *
 * - For stats by user (`/sleep/stats`)
 *
 * - For predictions:
 * - Efficiency next month (`/ai/predictions-next-month-efficiency`)
 */

fun main() {
    val logger = LoggerFactory.getLogger("Main")

    // =========================
    // Initialized DataSources
    // =========================

    val databaseEnabled = System.getenv("DB_ENABLED")?.toBooleanStrictOrNull() ?: true
    if (databaseEnabled) {
        try {
            AuthDataSource.init()
            if (!AuthDataSource.isConnectionHealthy()) logger.warn("Authentication database is unavailable; account endpoints will fail")
        } catch (e: Exception) {
            logger.error("Authentication database could not be initialized; account endpoints will fail", e)
        }
    } else {
        logger.warn("Authentication database disabled with DB_ENABLED=false")
    }

    val emailSecret = System.getenv("EMAIL_VERIFICATION_SECRET")
    if (emailSecret.isNullOrBlank() || emailSecret.length < 32) {
        logger.error("EMAIL_VERIFICATION_SECRET is not set or too short (< 32 chars). Registration will fail.")
    }

    // =========================
    // Start the Javalin web server
    // =========================

    val webSocketsEnabled = System.getenv("WEBSOCKETS_ENABLED")?.toBooleanStrictOrNull() ?: false
    val app = Javalin.create { config ->
        config.startup.showJavalinBanner = true
        config.http.maxRequestSize = 1_048_576L
        config.http.strictContentTypes = true
        config.bundledPlugins.enableCors { cors ->
            cors.addRule {
                WebOriginPolicy.allowedOrigins.forEach(it::allowHost)
                it.allowCredentials = true
            }
        }
        config.routes.beforeMatched(AccessManager::handleAccess)
        config.routes.before { ctx ->
            RequestSecurity.apply(ctx)
            ctx.contentType("application/json")
        }
        config.routes.exception(ValidationException::class.java) { e, ctx ->
            val err = e.errors.values.single().joinToString { it.message }
            ctx.result(err).status(400)
        }
        config.routes.exception(Exception::class.java) { e, ctx ->
            logger.error("Unhandled exception on ${ctx.method()} ${ctx.path()}", e)
            ctx.json(mapOf("error" to "Internal server error")).status(500)
        }
        config.routes.apiBuilder {
            // =========================
            // Endpoints
            // =========================
            get("/", { ctx -> ctx.json(mapOf("message" to "Server Javalin")) }, Role.SYSADMIN, Role.ADMIN, Role.CLIENT, Role.UNAUTHENTICATED)
            get("/health", { ctx -> ctx.json(mapOf("status" to "ok")) }, Role.UNAUTHENTICATED)
            // Auth endpoints
            path("auth") {
                post("login", AuthController::login, Role.UNAUTHENTICATED)
                post("register", RegistrationController::register, Role.UNAUTHENTICATED)
                post("verify", RegistrationController::verify, Role.UNAUTHENTICATED)
                get("session", AuthController::session, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
                post("logout", AuthController::logout, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
            }
            // CRUD account endpoints
            path("account") {
                get(UserAccountController::getAll, Role.SYSADMIN, Role.ADMIN)
                post(UserAccountController::create, Role.SYSADMIN, Role.ADMIN)
                path("{id}") {
                    get(UserAccountController::getOne, Role.SYSADMIN, Role.ADMIN)
                    delete(UserAccountController::delete, Role.SYSADMIN)
                    patch(UserAccountController::update, Role.SYSADMIN, Role.ADMIN)
                }
                path("userinfo") {
                    get("{username}", UserAccountController::getUserInfo, Role.SYSADMIN, Role.ADMIN)
                }
            }
            // Users info
            path("users") {
                get(UserController::getAllUsers, Role.SYSADMIN, Role.ADMIN)
                post("notify-update", { ctx ->
                    UserController.notifyUserUpdate()
                    ctx.status(200).json(mapOf("message" to "User update notification sent"))
                }, Role.SYSADMIN, Role.ADMIN)
            }
            // Sleep graphs endpoints
            path("sleep") {
                get("stats", SleepStatsController::getSleepStats, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
                get("states", SleepStateController::getCurrentSleepStates, Role.SYSADMIN, Role.ADMIN)
                post("states", SleepStateController::changeSleepState, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
                get("connections", SleepStateController::getConnectionStats, Role.SYSADMIN, Role.ADMIN)
            }
            // AI
            path("ai") {
                get("recommendation", SleepAiController::getRecommendation, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
                get("predictions-next-month-efficiency", SleepAiController::predictEfficiencyNextMonth, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
            }
            path("subscription") {
                get(SubscriptionController::current, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
                patch(SubscriptionController::update, Role.SYSADMIN, Role.ADMIN, Role.CLIENT)
            }
        }
        if (webSocketsEnabled) {
            config.routes.ws("/ws/users", UserController::configureWebSocket)
            config.routes.ws("/ws/sleep/mobile", SleepStateController::configureMobileWebSocket)
            config.routes.ws("/ws/sleep/dashboard", SleepStateController::configureDashboardWebSocket)
        }
    }.start("0.0.0.0", System.getenv("PORT")?.toIntOrNull() ?: 7070)

    if (webSocketsEnabled) {
        logger.warn("WebSockets enabled. Place the service behind an authenticated gateway before production use.")
    } else {
        logger.info("WebSockets disabled (WEBSOCKETS_ENABLED=false)")
    }

    logger.info("✔ Application started successfully")
}
