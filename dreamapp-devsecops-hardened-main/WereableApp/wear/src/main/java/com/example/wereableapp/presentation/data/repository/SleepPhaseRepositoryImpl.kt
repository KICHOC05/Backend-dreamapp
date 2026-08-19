package com.example.wereableapp.presentation.data.repository

import android.content.Context
import com.example.wereableapp.presentation.data.local.SleepPhaseDatabase
import com.example.wereableapp.presentation.data.local.entity.SleepPhaseDataEntity
import android.util.Log

class SleepPhaseRepositoryImpl(context: Context) : SleepPhaseRepository {

    private val dao = SleepPhaseDatabase.getDatabase(context).sleepPhaseDao()

    override suspend fun insert(record: SleepPhaseDataEntity) {
        Log.d("RoomInsert", "📝 Insertando fase: ${record.phase} - HR: ${record.bpm}")
        dao.insert(record)
    }

    override suspend fun getLast(): SleepPhaseDataEntity? {
        return dao.getAll().firstOrNull()
    }
}
