package team.dreamapp.com.presentation.controller.auth

import io.javalin.http.Context
import io.javalin.http.bodyValidator
import team.dreamapp.com.domain.usecase.auth.LoginUseCase
import team.dreamapp.com.domain.usecase.auth.LogoutUseCase
import team.dreamapp.com.infrastructure.service.auth.AuthServiceImpl
import team.dreamapp.com.presentation.dto.auth.LoginRequestDto
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.service.auth.AuthTokenService

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
            val userInfo = loginUseCase.execute(loginRequest.userName, loginRequest.password, "Cliente", ctx)
            val token = AuthTokenService.issue(userInfo)
            ctx.json(mapOf("success" to true, "data" to userInfo, "token" to token, "expiresIn" to 43200))
        } catch (ex: Exception) {
            logger.warn("Failed login attempt for supplied username")
            ctx.status(401).json(mapOf("success" to false, "error" to "Invalid credentials"))
        }
    }

    fun logout(ctx: Context) {
        AuthTokenService.revoke(ctx.header("Authorization")?.removePrefix("Bearer ")?.trim())
        val result = logoutUseCase.execute(ctx)
        ctx.json(mapOf("success" to result))
    }

}
