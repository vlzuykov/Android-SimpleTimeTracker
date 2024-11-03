package com.example.util.simpletimetracker.domain.resolver

import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData

interface BackupPartialRepo {

    suspend fun partialRestoreBackupFile(
        params: BackupOptionsData.Custom,
    ): ResultCode

    suspend fun readBackupFile(
        uriString: String,
    ): Pair<ResultCode, PartialBackupRestoreData?>
}