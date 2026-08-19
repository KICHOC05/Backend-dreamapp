package com.example.appmobile.domain.usecase

import com.example.appmobile.data.remote.api.SearchUserApiService
import com.example.appmobile.data.remote.dto.SearchUserResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson

class SearchUserUseCase {
    private val apiService: SearchUserApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://searchuser-nmry4bipxq-uc.a.run.app/")
            .client(okhttp3.OkHttpClient.Builder().addInterceptor(com.example.appmobile.data.remote.FirebaseAuthInterceptor()).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(SearchUserApiService::class.java)
    }
    
    suspend operator fun invoke(uidUser: String): Result<SearchUserResponse> {
        return try {
            val response = apiService.searchUser(uidUser)
            if (response.isSuccessful && response.body() != null) {
                // Respuesta exitosa (200) - usuario encontrado
                Result.success(response.body()!!)
            } else if (response.code() == 404 && response.errorBody() != null) {
                // 404 significa usuario no registrado - parsear el mensaje de error como respuesta válida
                try {
                    val errorBody = response.errorBody()?.string()
                    val gson = Gson()
                    val searchResponse = gson.fromJson(errorBody, SearchUserResponse::class.java)
                    Result.success(searchResponse)
                } catch (e: Exception) {
                    // Si no se puede parsear, crear respuesta por defecto
                    val searchResponse = SearchUserResponse(
                        message = "User has not completed the registration form",
                        status = false
                    )
                    Result.success(searchResponse)
                }
            } else {
                Result.failure(Exception("Search user failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
