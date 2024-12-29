package com.example.util.simpletimetracker.feature_notification.inactivity.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationInactivityBroadcastController @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
) {

    fun onInactivityReminder() = GlobalScope.launch {
        notificationInactivityInteractor.show()
        checkAndSchedule()
    }

    fun onBootCompleted() = GlobalScope.launch {
        checkAndSchedule()
    }

    private suspend fun checkAndSchedule() {
        if (prefsInteractor.getInactivityReminderRecurrent()) {
            notificationInactivityInteractor.checkAndSchedule()
        }
    }
}