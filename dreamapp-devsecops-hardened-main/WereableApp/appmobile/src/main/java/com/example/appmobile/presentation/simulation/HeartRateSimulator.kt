package com.example.appmobile.presentation.simulation

import com.example.appmobile.data.repository.HeartRateRepository
import kotlinx.coroutines.*
import kotlin.random.Random

object HeartRateSimulator {
    private var job: Job? = null

    fun startSimulation(scope: CoroutineScope) {
        if (job?.isActive == true) return  // Evita iniciar múltiples simulaciones

        job = scope.launch {
            while (isActive) {
                val simulatedBpm = Random.nextFloat() * (100 - 60) + 60  // BPM entre 60 y 100
                HeartRateRepository.updateBPM(simulatedBpm)
                delay(2000L)
            }
        }
    }

    fun stopSimulation() {
        job?.cancel()
    }
}
