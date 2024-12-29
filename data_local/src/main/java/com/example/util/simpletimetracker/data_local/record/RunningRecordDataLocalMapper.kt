package com.example.util.simpletimetracker.data_local.record

import com.example.util.simpletimetracker.domain.extension.dropMillis
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import javax.inject.Inject

class RunningRecordDataLocalMapper @Inject constructor() {

    fun map(dbo: RunningRecordWithRecordTagsDBO): RunningRecord {
        return RunningRecord(
            id = dbo.runningRecord.id,
            timeStarted = dbo.runningRecord.timeStarted,
            comment = dbo.runningRecord.comment,
            tagIds = dbo.recordTags.map { it.id },
        )
    }

    fun map(domain: RunningRecord): RunningRecordDBO {
        return RunningRecordDBO(
            id = domain.id,
            timeStarted = domain.timeStarted.dropMillis(),
            comment = domain.comment,
            tagId = 0,
        )
    }
}