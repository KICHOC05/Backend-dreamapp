package com.example.wereableapp.presentation.domain.usecase

import com.example.wereableapp.presentation.data.repository.UserRepository
import com.example.wereableapp.presentation.domain.model.HeartRate
import com.example.wereableapp.presentation.domain.model.HeartRateV
import com.example.wereableapp.presentation.domain.model.SleepPhase
import com.example.wereableapp.presentation.domain.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class SleepPhaseDetector(
    private val heartRateFlow: Flow<HeartRate?>,
    private val movementMagnitudeFlow: Flow<Double>,
    private val hrvFlow: Flow<HeartRateV?>,
    private val userDataFlow: StateFlow<UserData?> = UserRepository.userData // ← agregado
) {
    val sleepPhaseFlow: Flow<SleepPhase> = combine(
        heartRateFlow,
        movementMagnitudeFlow,
        hrvFlow,
        userDataFlow
    ) { hr, movement, hrv, user ->

        if (hr == null || hrv == null || user == null) {
            return@combine SleepPhase.LIGHT
        }

        val adjustedTable = adjustTableForUser(user) // ✅ Aquí user no es null

        detectSleepPhaseScored(
            bpm = hr.bpm,
            rmssd = hrv.rmssd,
            sdnn = hrv.sdnn,
            movement = movement,
            table = adjustedTable
        )
    }

}