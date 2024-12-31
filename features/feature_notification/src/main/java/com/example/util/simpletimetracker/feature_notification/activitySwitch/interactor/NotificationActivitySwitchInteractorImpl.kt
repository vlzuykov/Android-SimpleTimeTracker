package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.GetCurrentActivitySuggestionsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchManager
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationActivitySwitchParams
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_notification.core.NotificationCommonMapper
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class NotificationActivitySwitchInteractorImpl @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val manager: NotificationActivitySwitchManager,
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val timeMapper: TimeMapper,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val getNotificationActivitySwitchControlsInteractor: GetNotificationActivitySwitchControlsInteractor,
    private val recordInteractor: RecordInteractor,
    private val notificationCommonMapper: NotificationCommonMapper,
    private val getCurrentActivitySuggestionsInteractor: GetCurrentActivitySuggestionsInteractor,
) : NotificationActivitySwitchInteractor {

    override suspend fun updateNotification(
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
    ) {
        val shouldShow = prefsInteractor.getShowNotifications() &&
            prefsInteractor.getShowNotificationEvenWithNoTimers() &&
            runningRecordInteractor.isEmpty()

        if (shouldShow) {
            show(
                typesShift = typesShift,
                tagsShift = tagsShift,
                selectedTypeId = selectedTypeId,
            )
        } else {
            cancel()
        }
    }

    private suspend fun show(
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long,
    ) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val showRepeatButton = prefsInteractor.getEnableRepeatButton()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Day,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val viewedTags = if (selectedTypeId != 0L) {
            getSelectableTagsInteractor.execute(selectedTypeId)
                .filterNot { it.archived }
        } else {
            emptyList()
        }
        val runningRecords = runningRecordInteractor.getAll()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val recordTags = recordTagInteractor.getAll()
        val suggestions = getCurrentActivitySuggestionsInteractor.execute(
            recordTypesMap = recordTypes,
            runningRecords = runningRecords,
        )
        val prevRecord = if (retroactiveTrackingModeEnabled) {
            // TODO several previous?
            recordInteractor.getAllPrev(timeStarted = System.currentTimeMillis())
                .maxByOrNull { it.timeStarted }
        } else {
            null
        }
        val prevRecordType = prevRecord?.typeId?.let(recordTypes::get)
        val goals = filterGoalsByDayOfWeekInteractor.execute(
            goals = recordTypeGoalInteractor.getAllTypeGoals(),
            range = range,
            startOfDayShift = startOfDayShift,
        ).groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypes.keys.toList(),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }
        val controls = getNotificationActivitySwitchControlsInteractor.getControls(
            hint = "", // Replaced later.
            isDarkTheme = isDarkTheme,
            types = recordTypes.values.toList(),
            suggestions = suggestions,
            showRepeatButton = showRepeatButton,
            typesShift = typesShift,
            tags = viewedTags,
            tagsShift = tagsShift,
            selectedTypeId = selectedTypeId,
            goals = goals,
            allDailyCurrents = allDailyCurrents,
        )
        val hint: String
        val icon: RecordTypeIcon?
        val color: Int?
        val title: String
        val subtitle: String
        val untrackedTimeStarted: Long?
        val prevRecordDuration: Long?
        when {
            retroactiveTrackingModeEnabled && prevRecord != null && prevRecordType != null -> {
                hint = resourceRepo.getString(R.string.retroactive_tracking_mode_hint)
                icon = prevRecordType.icon.let(iconMapper::mapIcon)
                color = colorMapper.mapToColorInt(prevRecordType.color, isDarkTheme)
                val namePrefix = resourceRepo.getString(R.string.statistics_detail_last_record)
                val fullName = notificationCommonMapper.getNotificationText(
                    recordType = prevRecordType,
                    recordTags = recordTags.filter { it.id in prevRecord.tagIds },
                )
                title = "$namePrefix - $fullName"
                subtitle = timeMapper.formatTime(
                    time = prevRecord.timeEnded,
                    useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
                    showSeconds = prefsInteractor.getShowSeconds(),
                ).let { resourceRepo.getString(R.string.notification_time_ended, it) }
                untrackedTimeStarted = prevRecord.timeEnded
                prevRecordDuration = prevRecord.timeEnded - prevRecord.timeStarted
            }
            retroactiveTrackingModeEnabled -> {
                hint = ""
                icon = RecordTypeIcon.Image(R.drawable.unknown)
                color = colorMapper.toUntrackedColor(isDarkTheme)
                title = resourceRepo.getString(R.string.retroactive_tracking_mode_hint)
                subtitle = ""
                untrackedTimeStarted = null
                prevRecordDuration = null
            }
            else -> {
                hint = ""
                icon = RecordTypeIcon.Image(R.drawable.app_ic_launcher_monochrome)
                color = colorMapper.toUntrackedColor(isDarkTheme)
                title = resourceRepo.getString(R.string.running_records_empty)
                subtitle = ""
                untrackedTimeStarted = null
                prevRecordDuration = null
            }
        }

        NotificationActivitySwitchParams(
            icon = icon,
            color = color,
            title = title,
            subtitle = subtitle,
            untrackedStartedTimeStamp = untrackedTimeStarted,
            prevRecordDuration = prevRecordDuration,
            controls = when (controls) {
                is NotificationControlsParams.Disabled -> controls
                is NotificationControlsParams.Enabled -> controls.copy(hint = hint)
            },
        ).let(manager::show)
    }

    private fun cancel() {
        manager.hide()
    }
}