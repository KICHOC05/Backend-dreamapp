package com.example.appmobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_cycles")
data class SleepCycleEntity(
    @PrimaryKey val createdAt: Long, // Usamos createdAt como ID único
    val uidUser: String,
    val deviceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val timezone: String,
    val totalDuration: Int,
    val sleepDuration: Int,
    val lightSleepMinutes: Int,
    val deepSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeDuration: Int,
    val sleepEfficiency: Double,
    val awakeningsCount: Int,
    val quality: String,
    val avgHeartRate: Int,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgMovement: Int,
    val avgRmssd: Double,
    val avgSdnn: Double,
    val dataVersion: String
)
