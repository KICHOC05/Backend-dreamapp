package com.example.wereableapp.presentation.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wereableapp.presentation.data.local.dao.SleepPhaseDataDao
import com.example.wereableapp.presentation.data.local.entity.SleepPhaseDataEntity

@Database(
    entities = [SleepPhaseDataEntity::class],
    version = 2
)
abstract class SleepPhaseDatabase : RoomDatabase() {
    abstract fun sleepPhaseDao(): SleepPhaseDataDao
    companion object {
        @Volatile private var INSTANCE: SleepPhaseDatabase? = null

        fun getDatabase(context: Context): SleepPhaseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepPhaseDatabase::class.java,
                    "user_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
