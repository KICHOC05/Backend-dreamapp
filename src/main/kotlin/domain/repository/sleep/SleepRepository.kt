package team.dreamapp.com.domain.repository.sleep

import team.dreamapp.com.domain.model.sleep.SleepSummary

interface SleepRepository {
    fun getAllSleepSummaryByUser(uidUser: String): List<SleepSummary>
}