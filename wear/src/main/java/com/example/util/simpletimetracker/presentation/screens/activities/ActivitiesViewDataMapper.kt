/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.presentation.screens.activities

import androidx.compose.ui.graphics.toArgb
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.model.WearActivity
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.domain.model.WearCurrentActivity
import com.example.util.simpletimetracker.domain.model.WearLastRecord
import com.example.util.simpletimetracker.domain.model.WearSettings
import com.example.util.simpletimetracker.domain.model.WearTag
import com.example.util.simpletimetracker.presentation.theme.ColorInactive
import com.example.util.simpletimetracker.presentation.ui.components.ActivitiesListState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipState
import com.example.util.simpletimetracker.presentation.ui.components.ActivityChipType
import com.example.util.simpletimetracker.utils.orZero
import javax.inject.Inject

class ActivitiesViewDataMapper @Inject constructor(
    private val wearIconMapper: WearIconMapper,
    private val resourceRepo: WearResourceRepo,
) {

    fun mapErrorState(): ActivitiesListState.Error {
        return ActivitiesListState.Error(R.string.wear_loading_error)
    }

    fun mapEmptyState(): ActivitiesListState.Empty {
        return ActivitiesListState.Empty(R.string.record_types_empty)
    }

    // TODO maybe show suggestions as compact chips?
    fun mapContentState(
        activities: List<WearActivity>,
        currentActivities: List<WearCurrentActivity>,
        suggestionIds: List<Long>,
        lastRecords: List<WearLastRecord>,
        settings: WearSettings?,
        showCompactList: Boolean,
    ): ActivitiesListState.Content {
        val activitiesMap = activities.associateBy { it.id }
        val currentActivitiesMap = currentActivities.associateBy { it.id }
        val retroactiveModeEnabled = settings?.retroactiveTrackingMode == true
        val items = mutableListOf<ActivitiesListState.Content.Item>()

        val hint = if (retroactiveModeEnabled) {
            resourceRepo.getString(R.string.retroactive_tracking_mode_hint)
        } else {
            ""
        }

        if (retroactiveModeEnabled &&
            lastRecords.isNotEmpty()
        ) {
            items += mapUntrackedItem(lastRecords)
        }

        suggestionIds.mapNotNull { suggestion ->
            val activity = activitiesMap[suggestion]
                ?: return@mapNotNull null
            val lastId = suggestionIds.lastOrNull()
            val isLast = suggestion == lastId
            mapItem(
                activity = activity,
                currentActivitiesMap = currentActivitiesMap,
                lastRecords = lastRecords,
                showCompactList = showCompactList,
                retroactiveModeEnabled = retroactiveModeEnabled,
            ).copy(
                type = ActivityChipType.Suggestion(isLast),
            ).let {
                ActivitiesListState.Content.Item.Button(it)
            }
        }.takeIf {
            it.isNotEmpty()
        }?.let {
            items += it
            items += ActivitiesListState.Content.Item.Divider
        }

        if (settings?.enableRepeatButton == true) {
            items += mapRepeatItem()
        }

        items += activities.map { activity ->
            mapItem(
                activity = activity,
                currentActivitiesMap = currentActivitiesMap,
                lastRecords = lastRecords,
                showCompactList = showCompactList,
                retroactiveModeEnabled = retroactiveModeEnabled,
            ).let {
                ActivitiesListState.Content.Item.Button(it)
            }
        }

        return ActivitiesListState.Content(
            isCompact = showCompactList,
            hint = hint,
            items = items,
        )
    }

    private fun mapItem(
        activity: WearActivity,
        currentActivitiesMap: Map<Long, WearCurrentActivity>,
        lastRecords: List<WearLastRecord>,
        showCompactList: Boolean,
        retroactiveModeEnabled: Boolean,
    ): ActivityChipState {
        val currentActivity = currentActivitiesMap[activity.id]
        val lastRecord = if (retroactiveModeEnabled) {
            lastRecords.firstOrNull {
                it.activityId == activity.id
            }
        } else {
            null
        }

        val isCurrentTypeLast = lastRecord?.activityId == activity.id
        val isRunning: Boolean
        val timeHint: ActivityChipState.TimeHint?
        val timeHint2: ActivityChipState.TimeHint?
        val tagString: String
        val hint: String
        when {
            lastRecord != null && isCurrentTypeLast -> {
                val duration = ActivityChipState.TimeHint.Duration(lastRecord.finishedAt - lastRecord.startedAt)
                val untracked = ActivityChipState.TimeHint.Timer(lastRecord.finishedAt)
                isRunning = false
                timeHint = if (showCompactList) untracked else duration
                timeHint2 = if (showCompactList) duration else untracked
                tagString = mapTagString(lastRecord.tags)
                hint = resourceRepo.getString(R.string.statistics_detail_last_record)
            }
            currentActivity?.startedAt != null -> {
                isRunning = true
                timeHint = ActivityChipState.TimeHint.Timer(currentActivity.startedAt)
                timeHint2 = ActivityChipState.TimeHint.None
                tagString = mapTagString(currentActivity.tags)
                hint = ""
            }
            else -> {
                isRunning = false
                timeHint = ActivityChipState.TimeHint.None
                timeHint2 = ActivityChipState.TimeHint.None
                tagString = ""
                hint = ""
            }
        }

        return ActivityChipState(
            id = activity.id,
            name = activity.name,
            icon = wearIconMapper.mapIcon(activity.icon),
            color = activity.color,
            type = ActivityChipType.Base,
            isRunning = isRunning,
            timeHint = timeHint,
            timeHint2 = timeHint2,
            tagString = tagString,
            hint = hint,
        )
    }

    private fun mapRepeatItem(): ActivitiesListState.Content.Item {
        return ActivityChipState(
            id = REPEAT_ITEM_ID,
            name = resourceRepo.getString(R.string.running_records_repeat),
            icon = WearActivityIcon.Image(R.drawable.wear_repeat),
            color = ColorInactive.toArgb().toLong(),
            type = ActivityChipType.Repeat,
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }

    private fun mapUntrackedItem(
        lastRecords: List<WearLastRecord>,
    ): ActivitiesListState.Content.Item {
        val lastRecord = lastRecords.firstOrNull()
        val finishedAt = lastRecord?.finishedAt.orZero()
        return ActivityChipState(
            id = UNTRACKED_ITEM_ID,
            name = resourceRepo.getString(R.string.untracked_time_name),
            icon = WearActivityIcon.Image(R.drawable.app_unknown),
            color = ColorInactive.toArgb().toLong(),
            type = ActivityChipType.Untracked,
            timeHint = ActivityChipState.TimeHint.Timer(finishedAt),
        ).let {
            ActivitiesListState.Content.Item.Button(it)
        }
    }

    private fun mapTagString(tags: List<WearTag>?): String {
        return tags
            .orEmpty()
            .map { it.name }
            .takeUnless { it.isEmpty() }
            ?.joinToString(separator = ", ")
            .orEmpty()
    }

    companion object {
        private const val UNTRACKED_ITEM_ID = -1L
        private const val REPEAT_ITEM_ID = -2L
    }
}