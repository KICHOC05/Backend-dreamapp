package com.example.appmobile.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.appmobile.data.database.dao.SleepCycleDao
import com.example.appmobile.data.database.dao.SleepDataDao
import com.example.appmobile.data.database.dao.SleepPhaseDataDao
import com.example.appmobile.data.database.entity.SleepDataEntity
import com.example.appmobile.data.database.entity.SleepPhaseDataEntity
import com.example.appmobile.data.database.entity.SleepCycleEntity

@Database(
    entities = [
        SleepDataEntity::class,          // ya tenías
        SleepCycleEntity::class,         // nueva entidad para ciclos de sueño
        SleepPhaseDataEntity::class      // nueva entidad para fases de sueño
    ],
    version = 5,
    exportSchema = false
)
abstract class SleepDatabase : RoomDatabase() {

    abstract fun sleepDataDao(): SleepDataDao
    abstract fun sleepCycleDao(): SleepCycleDao
    abstract fun sleepPhaseDataDao(): SleepPhaseDataDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleep_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}