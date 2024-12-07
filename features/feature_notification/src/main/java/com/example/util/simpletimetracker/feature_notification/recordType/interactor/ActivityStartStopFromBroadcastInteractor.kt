package com.example.util.simpletimetracker.feature_notification.recordType.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager
import kotlinx.coroutines.delay
import javax.inject.Inject

class ActivityStartStopFromBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
) {

    suspend fun onActionActivityStop(
        typeId: Long,
    ) {
        val runningRecord = runningRecordInteractor.get(typeId)
            ?: return // Not running.

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
        )
    }

    suspend fun onActionTypeClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        if (selectedTypeId == REPEAT_BUTTON_ITEM_ID) {
            recordRepeatInteractor.repeat()
            return
        }

        val started = addRunningRecordMediator.tryStartTimer(
            typeId = selectedTypeId,
            // Switch controls are updated separately right from here,
            // so no need to update after record change.
            updateNotificationSwitch = false,
            commentInputAvailable = false, // TODO open activity?
        ) {
            // Update to show tag selection.
            update(
                from = from,
                typesShift = typesShift,
                tagsShift = 0,
                selectedTypeId = selectedTypeId,
            )
        }
        if (started) {
            val type = recordTypeInteractor.get(selectedTypeId)
            if (type?.defaultDuration.orZero() > 0) {
                completeTypesStateInteractor.notificationTypeIds += selectedTypeId
                update(from, typesShift)
                delay(500)
                completeTypesStateInteractor.notificationTypeIds -= selectedTypeId
                update(from, typesShift)
            } else {
                update(from, typesShift)
            }
        }
    }

    suspend fun onActionTagClick(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        addRunningRecordMediator.startTimer(
            typeId = selectedTypeId,
            comment = "",
            tagIds = listOfNotNull(tagId.takeUnless { it == 0L }),
        )
        if (from !is NotificationControlsManager.From.ActivitySwitch) {
            // Hide tag selection on current notification.
            // Switch would be hidden on start timer.
            update(from, typesShift)
        }
    }

    suspend fun onRequestUpdate(
        from: NotificationControlsManager.From,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
    ) {
        update(
            from = from,
            typesShift = typesShift,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
        )
    }

    private suspend fun update(
        from: NotificationControlsManager.From,
        typesShift: Int,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
    ) {
        when (from) {
            is NotificationControlsManager.From.ActivityNotification -> {
                val typeId = from.recordTypeId
                if (typeId == 0L) return
                notificationTypeInteractor.checkAndShow(
                    typeId = from.recordTypeId,
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                )
            }
            is NotificationControlsManager.From.ActivitySwitch -> {
                notificationActivitySwitchInteractor.updateNotification(
                    typesShift = typesShift,
                    tagsShift = tagsShift,
                    selectedTypeId = selectedTypeId,
                )
            }
        }
    }
}