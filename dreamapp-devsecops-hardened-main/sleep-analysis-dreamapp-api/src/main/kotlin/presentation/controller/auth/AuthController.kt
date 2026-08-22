package team.dreamapp.com.presentation.controller.auth

import io.javalin.http.Context
import io.javalin.http.bodyValidator
import team.dreamapp.com.domain.usecase.auth.LoginUseCase
import team.dreamapp.com.domain.usecase.auth.LogoutUseCase
import team.dreamapp.com.infrastructure.service.auth.AuthServiceImpl
import team.dreamapp.com.presentation.dto.auth.LoginRequestDto
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.service.auth.AuthSessionCookie
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService
import team.dreamapp.com.presentation.auth.AccessManager

object AuthController {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    private val loginUseCase = LoginUseCase(AuthServiceImpl())
    private val logoutUseCase = LogoutUseCase(AuthServiceImpl())

    fun login(ctx: Context) {
        val loginRequest = ctx.bodyValidator<LoginRequestDto>().get()
        if (loginRequest.userName.isBlank() || loginRequest.password.isBlank()) {
            ctx.status(400).json(mapOf("success" to false, "error" to "Required fields are missing"))
            return
        }
        try {
            val userInfo = loginUseCase.execute(loginRequest.userName, loginRequest.password, "Cliente")
            val token = AuthTokenService.issue(userInfo)
            AuthSessionCookie.set(ctx, token)
            val response = mutableMapOf<String, Any>(
                "success" to true,
                "data" to userInfo,
                "expiresIn" to AuthSessionCookie.MAX_AGE_SECONDS
            )
            val nonBrowserClient = ctx.header("Origin") == null && ctx.header("Sec-Fetch-Site") == null
            if (nonBrowserClient) response["token"] = token
            ctx.json(response)
        } catch (ex: Exception) {
            logger.warn("Failed login attempt for supplied username")
            ctx.status(401).json(mapOf("success" to false, "error" to "Invalid credentials"))
        }
    }

    fun logout(ctx: Context) {
        val bearer = ctx.header("Authorization")?.removePrefix("Bearer ")?.trim()
        AuthTokenService.revoke(bearer ?: AuthSessionCookie.read(ctx))
        AuthSessionCookie.clear(ctx)
        val result = logoutUseCase.execute()
        ctx.json(mapOf("success" to result))
    }

    fun session(ctx: Context) {
        ctx.json(mapOf("success" to true, "data" to AccessManager.currentUser(ctx)))
    }
}
