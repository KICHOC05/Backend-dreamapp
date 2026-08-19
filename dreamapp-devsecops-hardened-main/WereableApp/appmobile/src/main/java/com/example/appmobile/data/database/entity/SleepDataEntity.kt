package com.example.appmobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_sleep")
data class SleepDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val heartRate: Float?,
    val hrv: String?,
    val sleepPhase: String?,
    val timestamp: Long = System.currentTimeMillis()
)
