package com.example.appmobile.data.database.dao

import androidx.room.*
import com.example.appmobile.data.database.entity.UserDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert
    suspend fun insert(userData: UserDataEntity): Long

    @Query("SELECT * FROM user_data ORDER BY id DESC LIMIT 1")
    fun getLatest(): Flow<UserDataEntity>

    @Query("SELECT * FROM user_data ORDER BY id DESC LIMIT 1")
    suspend fun getLatestOnce(): UserDataEntity?
}
