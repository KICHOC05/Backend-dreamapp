package com.example.dashboardapp.di

import com.example.dashboardapp.data.remote.api.user.UserApiService
import com.example.dashboardapp.data.remote.api.auth.AuthApiService
import com.example.dashboardapp.data.repository.auth.AuthRepositoryImpl
import com.example.dashboardapp.domain.repository.auth.AuthRepository
import com.example.dashboardapp.domain.usecase.auth.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import androidx.room.Room
import com.example.dashboardapp.data.local.AppDatabase
import com.example.dashboardapp.data.local.dao.UserDao
import com.example.dashboardapp.domain.usecase.auth.LogoutUseCase
import com.example.dashboardapp.domain.usecase.auth.RegisterUseCase
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.dashboardapp.data.local.session.SessionManager
import com.example.dashboardapp.data.remote.api.sleep.SleepApiService
import com.example.dashboardapp.data.remote.api.sleep.SleepPredictionApiService
import com.example.dashboardapp.domain.repository.user.UserRepository
import com.example.dashboardapp.domain.usecase.user.GetAllUsersUseCase
import com.example.dashboardapp.data.remote.helpers.NotifyUpdateHelper
import com.example.dashboardapp.data.repository.sleep.SleepPredictionRepositoryImpl
import com.example.dashboardapp.data.repository.sleep.SleepRepositoryImpl
import com.example.dashboardapp.domain.repository.sleep.SleepPredictionRepository
import com.example.dashboardapp.domain.repository.sleep.SleepRepository
import com.example.dashboardapp.domain.usecase.sleep.GetPredictEfficiencyNextMonthUseCase
import com.example.dashboardapp.domain.usecase.sleep.GetSleepStatsByUserUseCase
import javax.inject.Named
import com.example.dashboardapp.BuildConfig

/**
 * DI Module - Dependency Injection with Hilt - Dagger
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val baseURL = BuildConfig.API_BASE_URL
    val baseURLWebSockets = BuildConfig.WS_BASE_URL

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager =
        SessionManager(context)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "dashboardapp_db"
    )
        .fallbackToDestructiveMigration()
        .build()

    // Web socket users Firebase
    @Provides
    @Singleton
    @Named("webSocketUrl")
    fun provideWebSocketUrl(): String = "$baseURLWebSockets/ws/users"

    // Web socket sleep states
    @Provides
    @Singleton
    @Named("sleepWebSocketUrl")
    fun provideSleepWebSocketUrl(): String = "$baseURLWebSockets/ws/sleep/dashboard"

    @Provides
    @Singleton
    @Named("notifyUpdateUrl")
    fun provideNotifyUpdateUrl(): String = "$baseURL/users/notify-update"

    @Provides
    @Singleton
    fun provideNotifyUpdateHelper(@Named("notifyUpdateUrl") notifyUpdateUrl: String): NotifyUpdateHelper = NotifyUpdateHelper(notifyUpdateUrl)

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApiService, userDao: UserDao): AuthRepository =
        AuthRepositoryImpl(api, userDao)

    @Provides
    @Singleton
    fun provideLoginUseCase(repository: AuthRepository): LoginUseCase =
        LoginUseCase(repository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(repository: AuthRepository): LogoutUseCase =
        LogoutUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(
        repository: AuthRepository,
        loginUseCase: LoginUseCase
    ): RegisterUseCase {
        return RegisterUseCase(repository, loginUseCase)
    }

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideGetAllUsersUseCase(
        repository: UserRepository
    ): GetAllUsersUseCase {
        return GetAllUsersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSleepApiService(retrofit: Retrofit): SleepApiService =
        retrofit.create(SleepApiService::class.java)

    @Provides
    @Singleton
    fun provideSleepRepository(api: SleepApiService): SleepRepository =
        SleepRepositoryImpl(api)

    @Provides
    @Singleton
    fun getSleepStatsByUserUseCase(
        repository: SleepRepository
    ) : GetSleepStatsByUserUseCase {
        return GetSleepStatsByUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSleepPredictionApiService(retrofit: Retrofit): SleepPredictionApiService =
        retrofit.create(SleepPredictionApiService::class.java)

    @Provides
    @Singleton
    fun provideSleepPredictionRepository(api: SleepPredictionApiService): SleepPredictionRepository =
        SleepPredictionRepositoryImpl(api)

    @Provides
    @Singleton
    fun getPredictEfficiencyNextMonthUseCase(
        repository: SleepPredictionRepository
    ) : GetPredictEfficiencyNextMonthUseCase {
       return GetPredictEfficiencyNextMonthUseCase(repository)
    }

}
