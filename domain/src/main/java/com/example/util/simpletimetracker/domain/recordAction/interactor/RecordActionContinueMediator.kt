package com.example.util.simpletimetracker.domain.recordAction.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import javax.inject.Inject

class RecordActionContinueMediator @Inject constructor(
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val removeRecordMediator: RemoveRecordMediator,
) {

    suspend fun execute(
        recordId: Long?,
        typeId: Long,
        timeStarted: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        // Remove current record if exist.
        recordId?.let {
            val oldTypeId = recordInteractor.get(it)?.typeId.orZero()
            removeRecordMediator.remove(it, oldTypeId)
        }
        // Stop same type running record if exist (only one of the same type can run at once).
        // Widgets will update on adding.
        runningRecordInteractor.get(typeId)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it, updateWidgets = false) }
        // Add new running record.
        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment,
            tagIds = tagIds,
            timeStarted = AddRunningRecordMediator.StartTime.Timestamp(timeStarted),
            checkDefaultDuration = false,
        )
    }
}