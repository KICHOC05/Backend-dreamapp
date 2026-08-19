package com.example.appmobile.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.appmobile.data.database.entity.SleepDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDataDao {
    
    @Insert
    suspend fun insertSleepData(sleepData: SleepDataEntity)
    
    @Query("SELECT * FROM data_sleep ORDER BY timestamp DESC LIMIT 20")
    fun getLast20Records(): Flow<List<SleepDataEntity>>
    
    @Query("SELECT * FROM data_sleep ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<SleepDataEntity>>
    
    @Query("DELETE FROM data_sleep")
    suspend fun deleteAll()
}
