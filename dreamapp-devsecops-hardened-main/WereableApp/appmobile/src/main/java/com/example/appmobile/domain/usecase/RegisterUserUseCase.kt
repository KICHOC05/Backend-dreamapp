package com.example.appmobile.domain.usecase

import com.example.appmobile.data.remote.api.RegisterUserApiService
import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import com.example.appmobile.data.remote.dto.RegisterUserResponseDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import android.util.Log

class RegisterUserUseCase {
    private val apiService: RegisterUserApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("RegisterUserUseCase", message)
        }.apply {
            level = if (com.example.appmobile.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(com.example.appmobile.data.remote.FirebaseAuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://registeruser-nmry4bipxq-uc.a.run.app/")
            .client(okHttpClient)
            // SIN GSON CONVERTER - SOLO ENVIAMOS JSON, NO PARSEAMOS RESPUESTA
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(RegisterUserApiService::class.java)
    }
    
    suspend operator fun invoke(
        uidUser: String,
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: String
    ): Result<RegisterUserResponseDto> {
        return try {
            val request = RegisterUserRequestDto(
                uidUser = uidUser,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                sex = sex
            )
            
            Log.d("RegisterUserUseCase", "Sending request: $request")
            
            val response = apiService.registerUser(request)
            
            Log.d("RegisterUserUseCase", "Response code: ${response.code()}")
            Log.d("RegisterUserUseCase", "Response message: ${response.message()}")
            Log.d("RegisterUserUseCase", "Is successful: ${response.isSuccessful}")
            
            if (response.code() == 201) {
                // 201 Created: Usuario registrado exitosamente
                // NO LEEMOS NADA DEL BODY - SOLO NAVEGAMOS
                response.body()?.close() // Cerramos el body sin leerlo
                val registerResponse = RegisterUserResponseDto(message = "User registered successfully")
                Log.d("RegisterUserUseCase", "201 SUCCESS - FORCING NAVIGATION TO PROFILE!")
                Result.success(registerResponse)
            } else if (response.code() == 400) {
                // 400: Error de validación (campos incorrectos)
                val message = try {
                    response.errorBody()?.string() ?: "Validation error"
                } catch (e: Exception) {
                    "Validation error"
                }
                Log.d("RegisterUserUseCase", "400 - Validation error: $message")
                Result.failure(Exception("Validation error: $message"))
            } else {
                Log.e("RegisterUserUseCase", "Request failed: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Register user failed: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("RegisterUserUseCase", "Exception during registration", e)
            Result.failure(e)
        }
    }
}
