package com.example.util.simpletimetracker.domain.pomodoro.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

class PomodoroStartInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val pomodoroCycleNotificationInteractor: PomodoroCycleNotificationInteractor,
) {

    suspend fun start() {
        val current = System.currentTimeMillis()
        prefsInteractor.setPomodoroModeStartedTimestampMs(current)
        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }

    suspend fun checkAndStart(typeId: Long) {
        if (!prefsInteractor.getEnablePomodoroMode()) return

        if (prefsInteractor.getPomodoroModeStartedTimestampMs() == 0L &&
            typeId in prefsInteractor.getAutostartPomodoroActivities()
        ) {
            start()
        }
    }
}