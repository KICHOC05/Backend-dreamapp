package com.example.wereableapp.presentation.domain.usecase

import com.example.wereableapp.presentation.domain.model.SleepPhase
import com.example.wereableapp.presentation.domain.model.UserData

data class SleepPhaseReference(
    val phase: SleepPhase,
    val bpmRange: ClosedRange<Float>,
    val rmssdRange: ClosedRange<Double>,
    val sdnnRange: ClosedRange<Double>,
    val movementRange: ClosedRange<Double>
)

val sleepPhaseTable = listOf(
    SleepPhaseReference(
        phase = SleepPhase.DEEP,
        bpmRange = 30f..49.9f,
        rmssdRange = 40.0..100.0,
        sdnnRange = 40.0..100.0,
        movementRange = 0.0..0.4
    ),
    SleepPhaseReference(
        phase = SleepPhase.REM,
        bpmRange = 50.0f..59.9f,
        rmssdRange = 30.0..39.9,
        sdnnRange = 30.0..39.9,
        movementRange = 0.1..1.0
    ),
    SleepPhaseReference(
        phase = SleepPhase.LIGHT,
        bpmRange = 60.0f..79.9f,
        rmssdRange = 10.0..29.9,
        sdnnRange = 10.0..29.9,
        movementRange = 0.5..2.0
    ),
    SleepPhaseReference(
        phase = SleepPhase.AWAKE,
        bpmRange = 80.0f..200.0f,
        rmssdRange = 0.0..9.9,
        sdnnRange = 0.0..9.9,
        movementRange = 1.5..10.0
    )
)

/**
 * Ajusta la tabla de referencia según la edad del usuario.
 */
fun adjustTableForUser(user: UserData): List<SleepPhaseReference> {
    val ageFactor = when {
        user.edad < 18 -> 0.95f
        user.edad in 18..40 -> 1.0f
        else -> 1.05f
    }

    return sleepPhaseTable.map { ref ->
        ref.copy(
            bpmRange = (ref.bpmRange.start * ageFactor)..(ref.bpmRange.endInclusive * ageFactor)
        )
    }
}

/**
 * Detecta la fase del sueño en base a un sistema de puntaje comparando cada métrica con la tabla.
 */
fun detectSleepPhaseScored(
    bpm: Float,
    rmssd: Double,
    sdnn: Double,
    movement: Double,
    table: List<SleepPhaseReference> = sleepPhaseTable
): SleepPhase {
    val scored = table.map { ref ->
        var score = 0
        if (bpm in ref.bpmRange) score++
        if (rmssd in ref.rmssdRange) score++
        if (sdnn in ref.sdnnRange) score++
        if (movement in ref.movementRange) score++
        ref.phase to score
    }

    return scored.maxByOrNull { it.second }?.first ?: SleepPhase.AWAKE
}
