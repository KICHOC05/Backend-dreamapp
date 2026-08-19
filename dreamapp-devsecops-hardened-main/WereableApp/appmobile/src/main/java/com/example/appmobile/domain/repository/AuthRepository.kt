package com.example.appmobile.domain.repository

import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import com.example.appmobile.data.remote.dto.RegisterUserResponseDto
import com.example.appmobile.data.remote.dto.SearchUserResponseDto

interface AuthRepository {
    suspend fun searchUser(uidUser: String): Result<SearchUserResponseDto>
    suspend fun registerUser(request: RegisterUserRequestDto): Result<RegisterUserResponseDto>
}
