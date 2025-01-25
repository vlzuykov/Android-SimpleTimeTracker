package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import javax.inject.Inject

class RecordQuickActionsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
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
                    newTagIds = getTagsAfterActivityChange(
                        currentTags = record.tagIds,
                        newTypeId = newTypeId,
                    )
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
                    newTagIds = getTagsAfterActivityChange(
                        currentTags = record.tagIds,
                        newTypeId = newTypeId,
                    )
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

    private suspend fun getTagsAfterActivityChange(
        currentTags: List<Long>,
        newTypeId: Long,
    ): List<Long> {
        val selectableTags = getSelectableTagsInteractor.execute(newTypeId)
            .map(RecordTag::id)
        return currentTags.filter { it in selectableTags }
    }
}