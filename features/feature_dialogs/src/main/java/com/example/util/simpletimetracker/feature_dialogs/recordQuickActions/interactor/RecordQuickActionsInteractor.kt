package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.domain.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.model.Record
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
                Record(
                    id = recordId,
                    typeId = newTypeId,
                    timeStarted = record.timeStarted,
                    timeEnded = record.timeEnded,
                    comment = record.comment,
                    tagIds = emptyList(), // Reset tags.
                ).let {
                    addRecordMediator.add(it)
                    externalViewsInteractor.onRecordChangeType(record.typeId)
                }
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
                // Widgets will update on adding.
                removeRunningRecordMediator.remove(
                    typeId = recordId,
                    updateWidgets = false,
                    updateNotificationSwitch = false,
                )
                addRunningRecordMediator.addAfterChange(
                    typeId = newTypeId,
                    timeStarted = record.timeStarted,
                    comment = record.comment,
                    tagIds = emptyList(), // Reset tags
                )
            }
        }
    }
}