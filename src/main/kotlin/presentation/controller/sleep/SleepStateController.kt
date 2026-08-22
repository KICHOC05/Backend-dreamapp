package team.dreamapp.com.presentation.controller.sleep

import io.javalin.http.Context
import io.javalin.websocket.WsConfig
import io.javalin.websocket.WsContext
import org.slf4j.LoggerFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import team.dreamapp.com.domain.entity.sleep.SleepState
import team.dreamapp.com.domain.entity.sleep.SleepStateEvent
import team.dreamapp.com.domain.entity.sleep.Location
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

object SleepStateController {

    private val logger = LoggerFactory.getLogger("SleepStateController")
    private val objectMapper = jacksonObjectMapper()
    
    // WebSocket connections separated by type
    private val mobileConnections = mutableSetOf<WsContext>()
    private val dashboardConnections = mutableSetOf<WsContext>()
    
    // Store current sleep states for each user
    private val userSleepStates = ConcurrentHashMap<String, SleepStateEvent>()

    /** WebSocket for mobile apps (to send sleep state changes). */
    fun configureMobileWebSocket(ws: WsConfig) {
        ws.onConnect { ctx ->
            mobileConnections.add(ctx)
            logger.info("Mobile WebSocket connected: ${ctx}")

            // Send current sleep states to the newly connected mobile client
            sendCurrentStatesToMobile(ctx)
        }

        ws.onMessage { ctx ->
            try {
                handleMobileMessage(ctx, ctx.message())
            } catch (e: Exception) {
                logger.error("Error handling mobile message: ${e.message}", e)
                ctx.send(objectMapper.writeValueAsString(mapOf(
                    "error" to "Invalid message format"
                )))
            }
        }

        ws.onClose { ctx ->
            mobileConnections.remove(ctx)
            logger.info("Mobile WebSocket disconnected: ${ctx}")
        }

        ws.onError { ctx ->
            mobileConnections.remove(ctx)
            logger.error("Mobile WebSocket error: ${ctx}")
        }
    }

    /** WebSocket for dashboards (to receive sleep state updates). */
    fun configureDashboardWebSocket(ws: WsConfig) {
        ws.onConnect { ctx ->
            dashboardConnections.add(ctx)
            logger.info("Dashboard WebSocket connected: ${ctx}")

            // Send all current sleep states to the newly connected dashboard
            sendAllStatesToDashboard(ctx)
        }

        ws.onClose { ctx ->
            dashboardConnections.remove(ctx)
            logger.info("Dashboard WebSocket disconnected: ${ctx}")
        }

        ws.onError { ctx ->
            dashboardConnections.remove(ctx)
            logger.error("Dashboard WebSocket error: ${ctx}")
        }
    }

    /**
     * Handle messages from mobile apps
     */
    private fun handleMobileMessage(ctx: WsContext, message: String) {
        logger.debug("Received sleep-state message from mobile")
        
        val messageMap = objectMapper.readValue<Map<String, Any>>(message)
        
        when (messageMap["action"]) {
            "changeSleepState" -> {
                val userId = messageMap["userId"] as? String 
                    ?: throw IllegalArgumentException("userId is required")
                val userName = messageMap["userName"] as? String 
                    ?: throw IllegalArgumentException("userName is required")
                val sleepStateStr = messageMap["sleepState"] as? String 
                    ?: throw IllegalArgumentException("sleepState is required")
                val deviceId = messageMap["deviceId"] as? String
                
                // Parse location if provided
                val locationMap = messageMap["location"] as? Map<String, Any>
                val location = if (locationMap != null) {
                    Location(
                        latitude = (locationMap["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (locationMap["longitude"] as? Number)?.toDouble() ?: 0.0,
                        address = locationMap["address"] as? String
                    )
                } else null
                
                val sleepState = SleepState.fromString(sleepStateStr)
                    ?: throw IllegalArgumentException("Invalid sleep state: $sleepStateStr")
                
                val event = SleepStateEvent(
                    userId = userId,
                    userName = userName,
                    sleepState = sleepState,
                    timestamp = LocalDateTime.now(),
                    deviceId = deviceId,
                    location = location
                )
                
                // Store the current state for this user
                userSleepStates[userId] = event
                
                // Send confirmation to mobile app
                ctx.send(objectMapper.writeValueAsString(mapOf(
                    "status" to "success",
                    "message" to "Sleep state updated successfully",
                    "sleepState" to sleepState.name,
                    "timestamp" to event.timestamp.toString()
                )))
                
                // Broadcast to all dashboard connections
                broadcastToDashboard(event)
                
                logger.info("Sleep state changed: $userId -> ${sleepState.name}")
            }
            
            "disconnect" -> {
                val userId = messageMap["userId"] as? String 
                    ?: throw IllegalArgumentException("userId is required")
                val userName = messageMap["userName"] as? String 
                    ?: throw IllegalArgumentException("userName is required")
                
                // Remove user's sleep state
                userSleepStates.remove(userId)
                
                // Send confirmation to mobile app
                ctx.send(objectMapper.writeValueAsString(mapOf(
                    "status" to "success",
                    "message" to "User disconnected successfully",
                    "userId" to userId
                )))
                
                // Broadcast disconnection to all dashboard connections
                broadcastUserDisconnection(userId, userName)
                
                logger.info("User disconnected: $userId ($userName)")
            }
            
            "getCurrentStates" -> {
                sendCurrentStatesToMobile(ctx)
            }
            
            else -> {
                ctx.send(objectMapper.writeValueAsString(mapOf(
                    "error" to "Unknown action: ${messageMap["action"]}"
                )))
            }
        }
    }

    /**
     * Send current sleep states to a specific mobile client
     */
    private fun sendCurrentStatesToMobile(ctx: WsContext) {
        val currentStates = userSleepStates.values.map { it.toMap() }
        ctx.send(objectMapper.writeValueAsString(mapOf(
            "action" to "currentStates",
            "states" to currentStates
        )))
    }

    /**
     * Send all current sleep states to a specific dashboard client
     */
    private fun sendAllStatesToDashboard(ctx: WsContext) {
        val allStates = userSleepStates.values.map { it.toMap() }
        ctx.send(objectMapper.writeValueAsString(mapOf(
            "action" to "allStates",
            "states" to allStates,
            "timestamp" to LocalDateTime.now().toString()
        )))
    }

    /**
     * Broadcast sleep state change to all dashboard connections
     */
    private fun broadcastToDashboard(event: SleepStateEvent) {
        val message = objectMapper.writeValueAsString(mapOf(
            "action" to "stateChange",
            "event" to event.toMap()
        ))
        
        dashboardConnections.forEach { ctx ->
            try {
                ctx.send(message)
            } catch (e: Exception) {
                logger.error("Error sending to dashboard: ${e.message}")
                dashboardConnections.remove(ctx)
            }
        }
        
        logger.info("Broadcasted sleep state change to ${dashboardConnections.size} dashboard clients")
    }

    /**
     * Broadcast user disconnection to all dashboard connections
     */
    private fun broadcastUserDisconnection(userId: String, userName: String) {
        val message = objectMapper.writeValueAsString(mapOf(
            "action" to "userDisconnected",
            "userId" to userId,
            "userName" to userName,
            "timestamp" to LocalDateTime.now().toString()
        ))
        
        dashboardConnections.forEach { ctx ->
            try {
                ctx.send(message)
            } catch (e: Exception) {
                logger.error("Error sending disconnection to dashboard: ${e.message}")
                dashboardConnections.remove(ctx)
            }
        }
        
        logger.info("Broadcasted user disconnection to ${dashboardConnections.size} dashboard clients: $userName")
    }

    /**
     * REST endpoint to get current sleep states
     */
    fun getCurrentSleepStates(ctx: Context) {
        try {
            val allStates = userSleepStates.values.map { it.toMap() }
            ctx.json(mapOf(
                "states" to allStates,
                "count" to allStates.size,
                "timestamp" to LocalDateTime.now().toString()
            ))
        } catch (ex: Exception) {
            logger.error("Error getting current sleep states: ${ex.message}", ex)
            ctx.status(500).json(mapOf("error" to "Internal server error"))
        }
    }

    /**
     * REST endpoint to change sleep state (alternative to WebSocket)
     */
    fun changeSleepState(ctx: Context) {
        try {
            val body = ctx.bodyAsClass(Map::class.java) as Map<String, Any>
            
            val userId = body["userId"] as? String 
                ?: throw IllegalArgumentException("userId is required")
            val userName = body["userName"] as? String 
                ?: throw IllegalArgumentException("userName is required")
            val sleepStateStr = body["sleepState"] as? String 
                ?: throw IllegalArgumentException("sleepState is required")
            val deviceId = body["deviceId"] as? String
            
            // Parse location if provided
            val locationMap = body["location"] as? Map<String, Any>
            val location = if (locationMap != null) {
                Location(
                    latitude = (locationMap["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (locationMap["longitude"] as? Number)?.toDouble() ?: 0.0,
                    address = locationMap["address"] as? String
                )
            } else null
            
            val sleepState = SleepState.fromString(sleepStateStr)
                ?: throw IllegalArgumentException("Invalid sleep state: $sleepStateStr")
            
            val event = SleepStateEvent(
                userId = userId,
                userName = userName,
                sleepState = sleepState,
                timestamp = LocalDateTime.now(),
                deviceId = deviceId,
                location = location
            )
            
            // Store the current state for this user
            userSleepStates[userId] = event
            
            // Broadcast to all dashboard connections
            broadcastToDashboard(event)
            
            ctx.json(mapOf(
                "status" to "success",
                "message" to "Sleep state updated successfully",
                "event" to event.toMap()
            ))
            
            logger.info("Sleep state changed via REST: $userId -> ${sleepState.name}")
        } catch (ex: Exception) {
            logger.error("Error changing sleep state: ${ex.message}", ex)
            ctx.status(400).json(mapOf("error" to (ex.message ?: "Unknown error")))
        }
    }

    /**
     * Get connection statistics
     */
    fun getConnectionStats(ctx: Context) {
        ctx.json(mapOf(
            "mobileConnections" to mobileConnections.size,
            "dashboardConnections" to dashboardConnections.size,
            "activeUsers" to userSleepStates.size,
            "timestamp" to LocalDateTime.now().toString()
        ))
    }
}
