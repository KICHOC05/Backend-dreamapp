package com.example.appmobile.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appmobile.data.database.entity.SleepCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepCycleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepCycle(cycle: SleepCycleEntity)

    @Query("SELECT * FROM sleep_cycles ORDER BY createdAt DESC")
    fun getAllCycles(): Flow<List<SleepCycleEntity>>

    @Query("DELETE FROM sleep_cycles")
    suspend fun deleteAllCycles()
}