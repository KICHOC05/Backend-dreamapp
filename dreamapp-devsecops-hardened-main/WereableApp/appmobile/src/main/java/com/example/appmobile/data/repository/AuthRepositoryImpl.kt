package com.example.appmobile.data.repository

import com.example.appmobile.data.remote.api.AuthApiService
import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import com.example.appmobile.data.remote.dto.RegisterUserResponseDto
import com.example.appmobile.data.remote.dto.SearchUserResponseDto
import com.example.appmobile.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService
) : AuthRepository {
    
    override suspend fun searchUser(uidUser: String): Result<SearchUserResponseDto> {
        return try {
            val response = authApiService.searchUser(uidUser)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Search user failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun registerUser(request: RegisterUserRequestDto): Result<RegisterUserResponseDto> {
        return try {
            val response = authApiService.registerUser(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Register user failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
