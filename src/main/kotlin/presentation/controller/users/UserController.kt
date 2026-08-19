package team.dreamapp.com.presentation.controller.users

import io.javalin.http.Context
import io.javalin.websocket.WsContext
import team.dreamapp.com.domain.usecase.users.GetAllUsersUseCase
import team.dreamapp.com.infrastructure.di.RepositoryProvider

import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object UserController {

    private val logger = LoggerFactory.getLogger("UserController")
    private val getAllUsersUseCase = GetAllUsersUseCase(RepositoryProvider.userRepository)

    private val userConnections = mutableSetOf<WsContext>()
    private val objectMapper = jacksonObjectMapper()

    // Register endpoint WebSocket in Javalin
    fun registerWebSocket(app: io.javalin.Javalin) {
        app.ws("/ws/users") { ws ->
            ws.onConnect { ctx ->
                userConnections.add(ctx)
                logger.info("WebSocket connect: $ctx")
                sendUsersToClient(ctx)
            }
            ws.onClose { ctx ->
                userConnections.remove(ctx)
                logger.info("WebSocket disconnect: $ctx")
            }
            ws.onError { ctx ->
                logger.error("WebSocket error: $ctx")
            }
        }
    }

    // Send user list to all users connectd
    fun notifyUserUpdate() {
        val users = getAllUsersUseCase()
        val json = objectMapper.writeValueAsString(users)
        userConnections.forEach { it.send(json) }
        logger.info("Usuarios enviados a ${userConnections.size} clientes WebSocket")
    }

    // Send to specific client
    private fun sendUsersToClient(ctx: WsContext) {
        val users = getAllUsersUseCase()
        val json = objectMapper.writeValueAsString(users)
        ctx.send(json)
    }

    fun getAllUsers(ctx: Context) {
        try {
            val users = getAllUsersUseCase()
            ctx.json(users)
        } catch (ex: Exception) {
            logger.error("Unable to retrieve users", ex)
            ctx.status(500).json(mapOf("error" to "Internal server error"))
        }
    }
}
