package com.example.appmobile.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_phase_data")
data class SleepPhaseDataEntity(
    @PrimaryKey
    val id: Int,
    val parentCreatedAt: Long, // Para relacionarlo con el ciclo completo
    val phase: String,
    val datetime: String,
    val hr_bpm: Int,
    val hrv_rmssd: Double,
    val hrv_sdnn: Double
)