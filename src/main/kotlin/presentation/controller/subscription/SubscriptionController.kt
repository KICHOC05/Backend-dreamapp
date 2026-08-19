package team.dreamapp.com.presentation.controller.subscription

import io.javalin.http.Context
import io.javalin.http.BadRequestResponse
import io.javalin.http.UnauthorizedResponse
import kotliquery.queryOf
import kotliquery.sessionOf
import team.dreamapp.com.infrastructure.datasouce.authdatabase.AuthDataSource
import team.dreamapp.com.presentation.auth.AccessManager.userInfo

object SubscriptionController {
    private val plans = setOf("FREE", "PLUS", "PRO")

    fun current(ctx: Context) {
        val user = ctx.userInfo ?: throw UnauthorizedResponse("Authentication required")
        val query = queryOf(
            "SELECT subscription_plan FROM user_account WHERE id = CAST(? AS UUID)", user.id
        ).map { it.string("subscription_plan") }.asSingle
        val plan = sessionOf(AuthDataSource.get()).use { it.run(query) } ?: "FREE"
        ctx.json(mapOf("success" to true, "plan" to plan))
    }

    fun update(ctx: Context) {
        val user = ctx.userInfo ?: throw UnauthorizedResponse("Authentication required")
        val requested = (ctx.bodyAsClass(Map::class.java)["plan"] as? String)?.uppercase()
            ?: throw BadRequestResponse("plan is required")
        if (requested !in plans) throw BadRequestResponse("Unknown subscription plan")
        val changed = sessionOf(AuthDataSource.get()).use {
            it.run(queryOf(
                "UPDATE user_account SET subscription_plan = ? WHERE id = CAST(? AS UUID)", requested, user.id
            ).asUpdate)
        }
        ctx.json(mapOf("success" to (changed > 0), "plan" to requested,
            "message" to "Plan actualizado. El cobro en línea se habilitará al conectar la pasarela de pagos."))
    }
}
