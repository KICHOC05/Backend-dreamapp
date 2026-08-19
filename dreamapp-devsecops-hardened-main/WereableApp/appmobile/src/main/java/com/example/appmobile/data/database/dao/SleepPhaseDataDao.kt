package com.example.appmobile.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appmobile.data.database.entity.SleepPhaseDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepPhaseDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepPhaseData(phases: List<SleepPhaseDataEntity>)

    @Query("SELECT * FROM sleep_phase_data WHERE parentCreatedAt = :createdAt ORDER BY id ASC")
    fun getPhasesForCycle(createdAt: Long): Flow<List<SleepPhaseDataEntity>>

    @Query("DELETE FROM sleep_phase_data")
    suspend fun deleteAllPhases()
}