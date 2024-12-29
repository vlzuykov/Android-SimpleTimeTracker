package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(DelicateCoroutinesApi::class)
@Singleton
class NotificationTypeBroadcastController @Inject constructor(
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationActivitySwitchInteractor: NotificationActivitySwitchInteractor,
    private val activityStartStopFromBroadcastInteractor: ActivityStartStopFromBroadcastInteractor,
    private val notificationControlsMapper: NotificationControlsMapper,
) {

    fun onActionActivityStop(
        typeId: Long,
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(
                typeId = typeId,
            )
        }
    }

    fun onActionTypeClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
    ) {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionTypeClick(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@launch,
                selectedTypeId = selectedTypeId,
                typesShift = typesShift,
            )
        }
    }

    fun onActionTagClick(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        tagId: Long,
        typesShift: Int,
    ) {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionTagClick(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    recordTypeId = typeId,
                ) ?: return@launch,
                selectedTypeId = selectedTypeId,
                tagId = tagId,
                typesShift = typesShift,
            )
        }
    }

    fun onRequestUpdate(
        from: Int,
        typeId: Long,
        selectedTypeId: Long,
        typesShift: Int,
        tagsShift: Int,
    ) {
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onRequestUpdate(
                from = notificationControlsMapper.mapExtraToFrom(
                    extra = from,
                    typeId,
                ) ?: return@launch,
                selectedTypeId = selectedTypeId,
                typesShift = typesShift,
                tagsShift = tagsShift,
            )
        }
    }

    fun onBootCompleted() {
        GlobalScope.launch {
            notificationTypeInteractor.updateNotifications()
            notificationActivitySwitchInteractor.updateNotification()
        }
    }
}