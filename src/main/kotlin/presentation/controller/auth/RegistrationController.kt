package team.dreamapp.com.presentation.controller.auth

import io.javalin.http.Context
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import team.dreamapp.com.infrastructure.datasouce.authdatabase.AuthDataSource
import team.dreamapp.com.infrastructure.service.email.EmailService
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.sql.SQLException
import java.time.Instant
import java.util.Locale

data class RegistrationRequest(
    val firstName: String = "",
    val lastName: String = "",
    val userName: String = "",
    val email: String = "",
    val password: String = ""
)

data class VerificationRequest(val email: String = "", val code: String = "")

object RegistrationController {
    private val logger = LoggerFactory.getLogger(RegistrationController::class.java)
    private val random = SecureRandom()
    private val usernamePattern = Regex("^[A-Za-z0-9._-]{3,40}$")
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun register(ctx: Context) {
        val request = runCatching { ctx.bodyAsClass(RegistrationRequest::class.java) }.getOrNull()
        if (request == null) return badRequest(ctx, "Datos de registro inválidos.")
        val firstName = request.firstName.trim().take(100)
        val lastName = request.lastName.trim().take(100)
        val username = request.userName.trim().lowercase(Locale.ROOT)
        val email = request.email.trim().lowercase(Locale.ROOT)
        if (firstName.length < 2 || lastName.length < 2) return badRequest(ctx, "Escribe tu nombre y apellido.")
        if (!usernamePattern.matches(username)) return badRequest(ctx, "El usuario debe tener de 3 a 40 caracteres válidos.")
        if (!emailPattern.matches(email) || email.length > 254) return badRequest(ctx, "Correo electrónico inválido.")
        if (!strongPassword(request.password)) return badRequest(ctx, "La contraseña debe tener 10 caracteres, mayúscula, minúscula y número.")

        val code = (random.nextInt(900_000) + 100_000).toString()
        val codeHash = hashCode(email, code)
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt(12))
        try {
            AuthDataSource.get().connection.use { connection ->
                connection.prepareStatement("SELECT 1 FROM user_account WHERE LOWER(username)=? OR LOWER(email)=?").use { statement ->
                    statement.setString(1, username); statement.setString(2, email)
                    if (statement.executeQuery().next()) { ctx.status(409).json(mapOf("success" to false, "error" to "El usuario o correo ya está registrado.")); return }
                }
                connection.prepareStatement("SELECT last_sent_at > CURRENT_TIMESTAMP - INTERVAL '60 seconds' FROM pending_registration WHERE email=?").use { statement ->
                    statement.setString(1, email)
                    val result = statement.executeQuery()
                    if (result.next() && result.getBoolean(1)) { ctx.status(429).json(mapOf("success" to false, "error" to "Espera un minuto antes de solicitar otro código.")); return }
                }
                connection.prepareStatement("""
                    INSERT INTO pending_registration(email, username, firstname, lastname, password_hash, code_hash, expires_at, attempts, last_sent_at)
                    VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP + INTERVAL '10 minutes', 0, CURRENT_TIMESTAMP)
                    ON CONFLICT (email) DO UPDATE SET username=EXCLUDED.username, firstname=EXCLUDED.firstname,
                      lastname=EXCLUDED.lastname, password_hash=EXCLUDED.password_hash, code_hash=EXCLUDED.code_hash,
                      expires_at=EXCLUDED.expires_at, attempts=0, last_sent_at=CURRENT_TIMESTAMP
                """.trimIndent()).use { statement ->
                    statement.setString(1, email); statement.setString(2, username); statement.setString(3, firstName)
                    statement.setString(4, lastName); statement.setString(5, passwordHash); statement.setString(6, codeHash)
                    statement.executeUpdate()
                }
            }
            try {
                EmailService.sendVerificationCode(email, code)
            } catch (ex: Exception) {
                AuthDataSource.get().connection.use { connection -> connection.prepareStatement("DELETE FROM pending_registration WHERE email=?").use { it.setString(1, email); it.executeUpdate() } }
                logger.error("Verification email could not be sent", ex)
                ctx.status(503).json(mapOf("success" to false, "error" to "No pudimos enviar el correo. Intenta nuevamente.")); return
            }
            ctx.status(202).json(mapOf("success" to true, "message" to "Te enviamos un código de 6 dígitos."))
        } catch (ex: SQLException) {
            logger.warn("Registration conflict or database error: {}", ex.sqlState)
            ctx.status(409).json(mapOf("success" to false, "error" to "El usuario o correo no está disponible."))
        }
    }

    fun verify(ctx: Context) {
        val request = runCatching { ctx.bodyAsClass(VerificationRequest::class.java) }.getOrNull()
        val email = request?.email?.trim()?.lowercase(Locale.ROOT).orEmpty()
        val code = request?.code?.trim().orEmpty()
        if (!emailPattern.matches(email) || !Regex("^\\d{6}$").matches(code)) return badRequest(ctx, "Código inválido.")
        AuthDataSource.get().connection.use { connection ->
            connection.autoCommit = false
            try {
                val pending = connection.prepareStatement("""
                    SELECT username, firstname, lastname, password_hash, code_hash, expires_at, attempts
                    FROM pending_registration WHERE email=? FOR UPDATE
                """.trimIndent()).use { statement ->
                    statement.setString(1, email)
                    statement.executeQuery().use { result ->
                        if (!result.next()) null else listOf(
                            result.getString("username"), result.getString("firstname"), result.getString("lastname"),
                            result.getString("password_hash"), result.getString("code_hash"), result.getTimestamp("expires_at").toInstant().toString(),
                            result.getInt("attempts").toString()
                        )
                    }
                }
                if (pending == null) { connection.rollback(); ctx.status(400).json(mapOf("success" to false, "error" to "Solicita un código nuevo.")); return }
                val attempts = pending[6].toInt()
                if (attempts >= 5 || Instant.parse(pending[5]).isBefore(Instant.now())) {
                    connection.prepareStatement("DELETE FROM pending_registration WHERE email=?").use { it.setString(1, email); it.executeUpdate() }
                    connection.commit()
                    ctx.status(400).json(mapOf("success" to false, "error" to "El código caducó. Solicita uno nuevo.")); return
                }
                if (!MessageDigest.isEqual(pending[4].toByteArray(), hashCode(email, code).toByteArray())) {
                    connection.prepareStatement("UPDATE pending_registration SET attempts=attempts+1 WHERE email=?").use { it.setString(1, email); it.executeUpdate() }
                    connection.commit()
                    ctx.status(400).json(mapOf("success" to false, "error" to "Código incorrecto.")); return
                }
                connection.prepareStatement("""
                    INSERT INTO user_account(id, username, firstname, lastname, user_password, user_roles,
                      mobile_phone, phone_office, phone_ext, email, is_active, email_verified)
                    VALUES (gen_random_uuid(), ?, ?, ?, ?, 'Cliente', '', '', '', ?, TRUE, TRUE)
                """.trimIndent()).use { statement ->
                    statement.setString(1, pending[0]); statement.setString(2, pending[1]); statement.setString(3, pending[2])
                    statement.setString(4, pending[3]); statement.setString(5, email); statement.executeUpdate()
                }
                connection.prepareStatement("DELETE FROM pending_registration WHERE email=?").use { it.setString(1, email); it.executeUpdate() }
                connection.commit()
                ctx.status(201).json(mapOf("success" to true, "message" to "Cuenta verificada. Ya puedes iniciar sesión."))
            } catch (ex: Exception) {
                connection.rollback(); logger.error("Could not verify registration", ex)
                ctx.status(409).json(mapOf("success" to false, "error" to "No se pudo completar el registro."))
            } finally { connection.autoCommit = true }
        }
    }

    private fun strongPassword(password: String) = password.length in 10..72 &&
        password.any(Char::isUpperCase) && password.any(Char::isLowerCase) && password.any(Char::isDigit)

    private fun hashCode(email: String, code: String): String {
        val secret = System.getenv("EMAIL_VERIFICATION_SECRET")?.takeIf { it.length >= 32 }
            ?: error("EMAIL_VERIFICATION_SECRET must contain at least 32 characters")
        return MessageDigest.getInstance("SHA-256")
            .digest("$secret:$email:$code".toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun badRequest(ctx: Context, message: String) {
        ctx.status(400).json(mapOf("success" to false, "error" to message))
    }
}
