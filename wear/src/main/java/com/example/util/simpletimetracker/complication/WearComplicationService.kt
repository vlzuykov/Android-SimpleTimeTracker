/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.complication

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.CountUpTimeReference
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeDifferenceComplicationText
import androidx.wear.watchface.complications.data.TimeDifferenceStyle
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.data.WearDataRepo
import com.example.util.simpletimetracker.data.WearIconMapper
import com.example.util.simpletimetracker.domain.model.WearActivityIcon
import com.example.util.simpletimetracker.utils.getMainStartIntent
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class WearComplicationService : SuspendingComplicationDataSourceService() {

    @Inject
    lateinit var wearDataRepo: WearDataRepo

    @Inject
    lateinit var iconMapper: WearIconMapper

    private val tag: String = WearComplicationService::class.java.name
    private val appIcon = R.drawable.app_ic_launcher_monochrome
    private val iconSizeDp = 20
    private val defaultText = "×"
    private val previewText = "Tracking"

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                getShortTextData(
                    startedAt = System.currentTimeMillis(),
                    activityName = previewText,
                    activityIcon = WearActivityIcon.Image(appIcon),
                    onClick = null,
                )
            }
            else -> {
                Log.d(tag, "Unexpected complication type $type")
                null
            }
        }
    }

    // Text is recommended to be max length of 7 chars.
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> buildShortTextData()
            else -> {
                Log.d(tag, "Unexpected complication type ${request.complicationType}")
                null
            }
        }
    }

    private suspend fun buildShortTextData(): ComplicationData {
        val settings = wearDataRepo.loadSettings(forceReload = false)
            .getOrNull()
        val activities = wearDataRepo.loadActivities(forceReload = false)
            .getOrNull().orEmpty()
        val currentState = wearDataRepo.loadCurrentActivities(forceReload = false)
            .getOrNull()
        val currentActivities = currentState?.currentActivities.orEmpty()
        val retroactiveModeEnabled = settings?.retroactiveTrackingMode == true

        val activityName: String?
        val activityIcon: WearActivityIcon?
        val startedAt: Long?

        if (retroactiveModeEnabled) {
            val lastRecord = currentState?.lastRecords?.maxByOrNull { it.startedAt }
            val lastRecordActivity = activities.firstOrNull { it.id == lastRecord?.activityId }
            activityName = lastRecordActivity?.name
            activityIcon = lastRecordActivity?.icon?.let(iconMapper::mapIcon)
            startedAt = lastRecord?.finishedAt
        } else {
            // Take most current activity.
            val currentActivity = currentActivities.maxByOrNull { it.startedAt }
            val activity = activities.firstOrNull { it.id == currentActivity?.id }
            activityName = if (currentActivities.size > 1) {
                "+${currentActivities.size - 1}"
            } else {
                activity?.name
            }
            activityIcon = activity?.icon?.let(iconMapper::mapIcon)
            startedAt = currentActivity?.startedAt
        }

        return getShortTextData(
            startedAt = startedAt,
            activityName = activityName,
            activityIcon = activityIcon,
            onClick = getMainStartIntent(this),
        )
    }

    private fun getShortTextData(
        startedAt: Long?,
        activityName: String?,
        activityIcon: WearActivityIcon?,
        onClick: PendingIntent?,
    ): ComplicationData {
        val text = if (startedAt != null) {
            // SHORT_DUAL_UNIT seems better,
            // but on samsung watch for example "1h 15m" is shown as just "2h"
            TimeDifferenceComplicationText.Builder(
                TimeDifferenceStyle.STOPWATCH,
                CountUpTimeReference(Instant.ofEpochMilli(startedAt)),
            ).build()
        } else {
            PlainComplicationText.Builder(text = defaultText).build()
        }

        val name = if (activityName != null) {
            PlainComplicationText.Builder(text = activityName).build()
        } else {
            null
        }

        val defaultIcon = WearActivityIcon.Image(appIcon)
        val icon = MonochromaticImage.Builder(
            Icon.createWithBitmap(getBitmap(activityIcon ?: defaultIcon)),
        ).build()

        return ShortTextComplicationData
            .Builder(
                text = text,
                contentDescription = ComplicationText.EMPTY,
            )
            .run {
                if (name != null) this.setTitle(name) else this
            }
            .setMonochromaticImage(icon)
            .setTapAction(onClick)
            .build()
    }

    private fun getBitmap(icon: WearActivityIcon): Bitmap {
        return WearIconView(this)
            .apply {
                itemIcon = icon
                measureExactly(iconSizeDp.dpToPx(this.context))
            }
            .getBitmapFromView()
    }
}