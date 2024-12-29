package com.example.util.simpletimetracker.domain.pomodoro.interactor

import com.example.util.simpletimetracker.domain.pomodoro.mapper.PomodoroCycleDurationsMapper
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

class PomodoroNextCycleInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val pomodoroCycleDurationsMapper: PomodoroCycleDurationsMapper,
    private val getPomodoroSettingsInteractor: GetPomodoroSettingsInteractor,
    private val pomodoroCycleNotificationInteractor: PomodoroCycleNotificationInteractor,
) {

    suspend fun execute() {
        if (!prefsInteractor.getEnablePomodoroMode()) return
        val timeStartedMs = prefsInteractor.getPomodoroModeStartedTimestampMs()
        if (timeStartedMs == 0L) return

        val result = pomodoroCycleDurationsMapper.map(
            timeStartedMs = timeStartedMs,
            settings = getPomodoroSettingsInteractor.execute(),
        )

        val newTimeStarted = timeStartedMs +
            result.currentCycleDurationMs -
            result.cycleDurationMs + 1 // Just in case.
        prefsInteractor.setPomodoroModeStartedTimestampMs(newTimeStarted)

        pomodoroCycleNotificationInteractor.checkAndReschedule()
    }
}