package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.domain.recordTag.model.RunningRecordToRecordTag
import javax.inject.Inject

class RunningRecordToRecordTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RunningRecordToRecordTagDBO): RunningRecordToRecordTag {
        return RunningRecordToRecordTag(
            runningRecordId = dbo.runningRecordId,
            recordTagId = dbo.recordTagId,
        )
    }

    fun map(recordId: Long, recordTagId: Long): RunningRecordToRecordTagDBO {
        return RunningRecordToRecordTagDBO(
            runningRecordId = recordId,
            recordTagId = recordTagId,
        )
    }

    fun map(domain: RunningRecordToRecordTag): RunningRecordToRecordTagDBO {
        return RunningRecordToRecordTagDBO(
            runningRecordId = domain.runningRecordId,
            recordTagId = domain.recordTagId,
        )
    }
}