package com.example.appmobile.domain.usecase

import android.util.Log
import com.example.appmobile.data.remote.model.GetUserByUidResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface GetUserByUidApiService {
    @GET("getUserByUid")
    suspend fun getUserByUid(@Query("uidUser") uidUser: String): Response<GetUserByUidResponse>
}

class GetUserByUidUseCase {
    private val apiService: GetUserByUidApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://getuserbyuid-nmry4bipxq-uc.a.run.app/")
            .client(okhttp3.OkHttpClient.Builder().addInterceptor(com.example.appmobile.data.remote.FirebaseAuthInterceptor()).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(GetUserByUidApiService::class.java)
    }
    
    suspend operator fun invoke(uidUser: String): Result<GetUserByUidResponse> {
        return try {
            Log.d("GetUserByUidUseCase", "🌐 Llamando API con UID: $uidUser")
            Log.d("GetUserByUidUseCase", "🌐 URL completa: https://getuserbyuid-nmry4bipxq-uc.a.run.app?uidUser=$uidUser")
            
            val response = apiService.getUserByUid(uidUser)
            
            Log.d("GetUserByUidUseCase", "📱 Response code: ${response.code()}")
            Log.d("GetUserByUidUseCase", "📱 Response message: ${response.message()}")
            Log.d("GetUserByUidUseCase", "📱 Response headers: ${response.headers()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("GetUserByUidUseCase", "📦 Response body: $body")
                
                if (body != null) {
                    Log.d("GetUserByUidUseCase", "✅ Datos parseados correctamente: $body")
                    Result.success(body)
                } else {
                    Log.e("GetUserByUidUseCase", "❌ Response body es null")
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GetUserByUidUseCase", "❌ Error response: $errorBody")
                Result.failure(Exception("Get user failed: ${response.code()} - ${response.message()}. Error: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("GetUserByUidUseCase", "❌ Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
