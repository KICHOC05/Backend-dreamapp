package team.dreamapp.com.presentation.controller.account

import io.javalin.http.Context
import io.javalin.http.bodyValidator
import team.dreamapp.com.domain.entity.account.UserAccount
import team.dreamapp.com.domain.usecase.account.DeleteUserAccountUseCase
import team.dreamapp.com.domain.usecase.account.GetAllUserAccountUseCase
import team.dreamapp.com.domain.usecase.account.GetByUUIDUserAccountUseCase
import team.dreamapp.com.domain.usecase.account.InsertUserAccountUseCase
import team.dreamapp.com.domain.usecase.account.UpdateUserAccountUseCase
import team.dreamapp.com.domain.usecase.account.UserInfoByUseCase
import team.dreamapp.com.infrastructure.Util
import team.dreamapp.com.infrastructure.Util.properTrim
import team.dreamapp.com.infrastructure.di.RepositoryProvider
import team.dreamapp.com.presentation.dto.ApiResponse

object UserAccountController {

    private val insertUserAccountUseCase = InsertUserAccountUseCase(RepositoryProvider.userAccountRepository)
    private val deleteUserAccountUseCase = DeleteUserAccountUseCase(RepositoryProvider.userAccountRepository)
    private val updateUserAccountUseCase = UpdateUserAccountUseCase(RepositoryProvider.userAccountRepository)
    private val getAllUserAccountUseCase = GetAllUserAccountUseCase(RepositoryProvider.userAccountRepository)
    private val getByUUIDUserAccountUseCase = GetByUUIDUserAccountUseCase(RepositoryProvider.userAccountRepository)
    private val userInfoByUseCase = UserInfoByUseCase(RepositoryProvider.userAccountRepository)

    fun create(ctx: Context) {
        val userAccount = ctx.bodyValidator<UserAccount>().get().apply {
            id = Util.randomUUID()
            firstName = firstName.properTrim()
            lastName = lastName.properTrim()
            userName = userName.properTrim("")
            password = Util.hashPwd(password)
        }
        val result = insertUserAccountUseCase(userAccount)
        if (result == "failed") {
            ctx.status(500).json(ApiResponse(success = false, data = null, error = "No se pudo crear el usuario"))
        } else {
            ctx.json(ApiResponse(success = true, data = result))
        }
    }

    fun getAll(ctx: Context) {
        val where = ""
        val result = getAllUserAccountUseCase(where)
        ctx.json(ApiResponse(success = true, data = result))
    }

    fun getOne(ctx: Context) {
        val resourceId = ctx.pathParam("id")
        val result = getByUUIDUserAccountUseCase(resourceId)
        ctx.json(ApiResponse(success = true, data = result))
    }

    fun update(ctx: Context) {
        val resourceId = ctx.pathParam("id")
        val userAccount = ctx.bodyValidator<UserAccount>().get().apply {
            id = resourceId
            firstName = firstName.properTrim()
            lastName = lastName.properTrim()
            userName = userName.properTrim("")
        }
        val result = updateUserAccountUseCase(userAccount)
        ctx.json(ApiResponse(success = true, data = result))
    }

    fun delete(ctx: Context) {
        val resourceId = ctx.pathParam("id")
        val result = deleteUserAccountUseCase(resourceId)
        ctx.json(ApiResponse(success = true, data = result))
    }
    fun getUserInfo(ctx: Context) {
        val userName = ctx.pathParam("username")
        val result = userInfoByUseCase("username", userName)
        ctx.json(ApiResponse(success = true, data = result))
    }

}