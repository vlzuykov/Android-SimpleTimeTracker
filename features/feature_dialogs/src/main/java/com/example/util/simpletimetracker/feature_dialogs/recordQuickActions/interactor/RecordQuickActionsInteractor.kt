package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import javax.inject.Inject

class RecordQuickActionsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
) {

    suspend fun changeType(
        params: Type,
        newTypeId: Long,
    ) {
        when (params) {
            is Type.RecordTracked -> {
                val recordId = params.id
                val record = recordInteractor.get(recordId) ?: return
                if (record.typeId == newTypeId) return
                changeRecord(
                    old = record,
                    newTypeId = newTypeId,
                    newTagIds = emptyList(), // Reset tags.
                )
            }
            is Type.RecordUntracked -> {
                val newTimeStarted = params.timeStarted
                val newTimeEnded = params.timeEnded
                Record(
                    id = 0L,
                    typeId = newTypeId,
                    timeStarted = newTimeStarted,
                    timeEnded = newTimeEnded,
                    comment = "",
                    tagIds = emptyList(),
                ).let {
                    addRecordMediator.add(it)
                }
            }
            is Type.RecordRunning -> {
                val recordId = params.id
                val record = runningRecordInteractor.get(recordId) ?: return
                changeRunningRecord(
                    old = record,
                    newTypeId = newTypeId,
                    newTagIds = emptyList(), // Reset tags
                )
            }
        }
    }

    suspend fun changeTags(
        params: Type,
        newTagIds: List<Long>,
    ) {
        when (params) {
            is Type.RecordTracked -> {
                val recordId = params.id
                val record = recordInteractor.get(recordId) ?: return
                if (record.tagIds.sorted() == newTagIds.sorted()) return
                changeRecord(
                    old = record,
                    newTypeId = record.typeId,
                    newTagIds = newTagIds,
                )
            }
            is Type.RecordUntracked -> {
                // Do nothing. Should not be possible.
            }
            is Type.RecordRunning -> {
                val recordId = params.id
                val record = runningRecordInteractor.get(recordId) ?: return
                changeRunningRecord(
                    old = record,
                    newTypeId = recordId,
                    newTagIds = newTagIds,
                )
            }
        }
    }

    private suspend fun changeRecord(
        old: Record,
        newTypeId: Long,
        newTagIds: List<Long>,
    ) {
        old.copy(
            typeId = newTypeId,
            tagIds = newTagIds,
        ).let {
            addRecordMediator.add(it)
            if (old.typeId != newTypeId) {
                externalViewsInteractor.onRecordChangeType(old.typeId)
            }
        }
    }

    private suspend fun changeRunningRecord(
        old: RunningRecord,
        newTypeId: Long,
        newTagIds: List<Long>,
    ) {
        // Widgets will update on adding.
        removeRunningRecordMediator.remove(
            typeId = old.id,
            updateWidgets = false,
            updateNotificationSwitch = false,
        )
        addRunningRecordMediator.addAfterChange(
            typeId = newTypeId,
            timeStarted = old.timeStarted,
            comment = old.comment,
            tagIds = newTagIds,
        )
    }
}