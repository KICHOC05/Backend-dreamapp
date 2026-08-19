package com.example.appmobile.data.repository

import com.example.appmobile.data.database.dao.SleepDataDao
import com.example.appmobile.data.database.entity.SleepDataEntity
import kotlinx.coroutines.flow.Flow

class SleepDataRepository(private val sleepDataDao: SleepDataDao) {
    
    suspend fun insertSleepData(heartRate: Float?, hrv: String?, sleepPhase: String?) {
        val sleepData = SleepDataEntity(
            heartRate = heartRate,
            hrv = hrv,
            sleepPhase = sleepPhase
        )
        sleepDataDao.insertSleepData(sleepData)
    }
    
    fun getLast20Records(): Flow<List<SleepDataEntity>> {
        return sleepDataDao.getLast20Records()
    }
    
    fun getAllRecords(): Flow<List<SleepDataEntity>> {
        return sleepDataDao.getAllRecords()
    }
    
    suspend fun deleteAll() {
        sleepDataDao.deleteAll()
    }
}
