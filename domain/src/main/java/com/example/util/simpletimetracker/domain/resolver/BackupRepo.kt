package com.example.util.simpletimetracker.domain.resolver

import com.example.util.simpletimetracker.domain.model.BackupOptionsData

interface BackupRepo {

    suspend fun saveBackupFile(
        uriString: String,
        params: BackupOptionsData.Save,
    ): ResultCode

    suspend fun restoreBackupFile(
        uriString: String,
        params: BackupOptionsData.Restore,
    ): ResultCode
}