package com.example.util.simpletimetracker.data_local.record

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordTag.RecordTagDBO
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagDBO

data class RunningRecordWithRecordTagsDBO(
    @Embedded
    val runningRecord: RunningRecordDBO,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = RecordTagDBO::class,
        associateBy = Junction(
            RunningRecordToRecordTagDBO::class,
            parentColumn = "running_record_id",
            entityColumn = "record_tag_id",
        ),
    )
    val recordTags: List<RecordTagDBO>,
)