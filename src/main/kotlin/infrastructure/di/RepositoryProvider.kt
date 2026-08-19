package team.dreamapp.com.infrastructure.di

import team.dreamapp.com.domain.repository.account.UserAccountRepository
import team.dreamapp.com.domain.repository.sleep.SleepRepository
import team.dreamapp.com.domain.repository.users.UserRepository
import team.dreamapp.com.infrastructure.config.Config
import team.dreamapp.com.infrastructure.repository.UserAccountRepositoryImpl
import team.dreamapp.com.infrastructure.repository.sleep.SleepRepositoryImpl
import team.dreamapp.com.infrastructure.repository.users.UserRepositoryImpl

import java.net.http.HttpClient

object RepositoryProvider {
    // Auth Provider
    val userAccountRepository: UserAccountRepository = UserAccountRepositoryImpl()

    // Shared HttpClient instance
    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    // Base URL for user repository (Cloud Functions endpoint)
    private val baseURL: String = Config.SVR_FIRESTORE_CONF.firestoreFunctionsURL

    // Users Firebase Provider
    val userRepository: UserRepository = UserRepositoryImpl(httpClient, baseURL)

    // Stats Provider
    val sleepRepository: SleepRepository = SleepRepositoryImpl(httpClient, baseURL)
}
