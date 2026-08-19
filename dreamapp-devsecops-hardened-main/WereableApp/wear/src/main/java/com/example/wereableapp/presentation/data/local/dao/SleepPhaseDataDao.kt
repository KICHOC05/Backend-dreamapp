package com.example.wereableapp.presentation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.wereableapp.presentation.data.local.entity.SleepPhaseDataEntity

@Dao
interface SleepPhaseDataDao {
    @Insert
    suspend fun insert(record: SleepPhaseDataEntity)

    @Query("SELECT * FROM sleep_phase ORDER BY timestamp DESC")
    suspend fun getAll(): List<SleepPhaseDataEntity>
}
