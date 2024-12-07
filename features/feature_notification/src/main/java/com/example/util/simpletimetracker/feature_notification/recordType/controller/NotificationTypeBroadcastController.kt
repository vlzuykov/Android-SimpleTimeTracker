package com.example.util.simpletimetracker.feature_notification.recordType.controller

import com.example.util.simpletimetracker.domain.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartStopFromBroadcastInteractor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private val mutex = Mutex()

    fun onActionExternalActivityStart(
        name: String?,
        comment: String?,
        tagNames: List<String>,
        timeStarted: String?,
    ) = GlobalScope.launch {
        name ?: return@launch
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityStart(
                name = name,
                comment = comment,
                tagNames = tagNames,
                timeStarted = timeStarted,
            )
        }
    }

    fun onActionExternalActivityStop(
        name: String?,
        timeEnded: String?,
    ) = GlobalScope.launch {
        name ?: return@launch
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityStopByName(
                name = name,
                timeEnded = timeEnded,
            )
        }
    }

    fun onActionActivityStop(
        typeId: Long,
    ) {
        if (typeId == 0L) return
        GlobalScope.launch {
            activityStartStopFromBroadcastInteractor.onActionActivityStop(
                typeId = typeId,
                timeEnded = null,
            )
        }
    }

    fun onActionExternalActivityStopAll() = GlobalScope.launch {
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityStopAll()
        }
    }

    fun onActionExternalActivityStopShortest() = GlobalScope.launch {
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityStopShortest()
        }
    }

    fun onActionExternalActivityStopLongest() = GlobalScope.launch {
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityStopLongest()
        }
    }

    fun onActionExternalActivityRestart(
        comment: String?,
        tagNames: List<String>,
    ) = GlobalScope.launch {
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onActionActivityRestart(
                comment = comment, tagNames = tagNames,
            )
        }
    }

    fun onActionExternalRecordAdd(
        name: String?,
        timeStarted: String?,
        timeEnded: String?,
        comment: String?,
        tagNames: List<String>,
    ) = GlobalScope.launch {
        name ?: return@launch
        timeStarted ?: return@launch
        timeEnded ?: return@launch
        mutex.withLock {
            activityStartStopFromBroadcastInteractor.onRecordAdd(
                name = name,
                timeStarted = timeStarted,
                timeEnded = timeEnded,
                comment = comment,
                tagNames = tagNames,
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