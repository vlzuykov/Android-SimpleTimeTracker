package com.example.util.simpletimetracker.domain.backup.interactor

interface AutomaticBackupInteractor {

    fun schedule()

    fun cancel()

    fun onFinished()

    suspend fun backup()
}