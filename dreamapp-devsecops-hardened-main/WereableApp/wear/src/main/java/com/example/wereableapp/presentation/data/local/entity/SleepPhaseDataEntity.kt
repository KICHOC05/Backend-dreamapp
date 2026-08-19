package com.example.wereableapp.presentation.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_phase")
data class SleepPhaseDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val phase: String, // SleepPhase.name
    val bpm: Float,
    val rmssd: Double,
    val sdnn: Double,
    val movement: Double
)
