package com.example.util.simpletimetracker.feature_notification.automaticBackup.controller

import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticBackupInteractor
import javax.inject.Inject

class AutomaticBackupBroadcastController @Inject constructor(
    private val automaticBackupInteractor: AutomaticBackupInteractor,
) {

    suspend fun onReminder() {
        automaticBackupInteractor.backup()
    }

    fun onFinished() {
        automaticBackupInteractor.onFinished()
    }

    fun onBootCompleted() {
        automaticBackupInteractor.schedule()
    }
}