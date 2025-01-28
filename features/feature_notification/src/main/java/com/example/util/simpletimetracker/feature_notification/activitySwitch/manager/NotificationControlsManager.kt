package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationControlsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationControlsMapper: NotificationControlsMapper,
) {

    private val iconView = allowVmViolations {
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    }
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }

    fun getControlsView(
        from: From,
        controls: NotificationControlsParams,
        isBig: Boolean,
    ): RemoteViews? {
        if (controls !is NotificationControlsParams.Enabled) return null
        if (!isBig) return null

        val tagsControlsVisible: Boolean = controls.tags.isNotEmpty()

        return RemoteViews(context.packageName, R.layout.notification_switch_controls_layout).apply {
            val hintVisibility = if (controls.hint.isNotEmpty()) View.VISIBLE else View.GONE
            setViewVisibility(R.id.tvNotificationControlsHint, hintVisibility)
            setTextViewText(R.id.tvNotificationControlsHint, controls.hint)

            addTypeControls(from, controls)
            if (tagsControlsVisible) addTagControls(from, controls)

            val tagsControlsVisibility = if (tagsControlsVisible) View.VISIBLE else View.GONE
            setViewVisibility(R.id.containerNotificationTags, tagsControlsVisibility)
            setViewVisibility(R.id.containerNotificationTagsPrev, tagsControlsVisibility)
            setViewVisibility(R.id.containerNotificationTagsNext, tagsControlsVisibility)
        }
    }

    private fun RemoteViews.addTypeControls(
        from: From,
        params: NotificationControlsParams.Enabled,
    ) {
        // Prev button
        setImageViewBitmap(
            R.id.ivNotificationTypesPrev,
            getIconBitmap(params.controlIconPrev, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesPrev,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TYPES_PREV,
                requestCode = getRequestCode(from),
                from = from,
                recordTypesShift = (params.typesShift - TYPES_LIST_SIZE)
                    .coerceAtLeast(0),
            ),
        )

        // Types buttons
        val currentTypes = params.types.drop(params.typesShift).take(TYPES_LIST_SIZE)

        fun addPresentType(data: NotificationControlsParams.Type.Present) {
            val recordTypeId = (from as? From.ActivityNotification)?.recordTypeId
            val action = when (from) {
                is From.ActivityNotification -> {
                    if (data.id == recordTypeId) {
                        ACTION_NOTIFICATION_CONTROLS_STOP
                    } else {
                        ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK
                    }
                }
                is From.ActivitySwitch -> {
                    ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK
                }
            }
            val color = if (recordTypeId == data.id) {
                params.filteredTypeColor
            } else {
                data.color
            }
            getTypeControlView(
                icon = data.icon,
                color = color,
                checkState = data.checkState,
                isComplete = data.isComplete,
                intent = getPendingSelfIntent(
                    context = context,
                    action = action,
                    requestCode = getRequestCode(
                        from = from,
                        additionalInfo = RequestCode.AdditionalInfo.TypeId(data.id),
                    ),
                    from = from,
                    recordTypesShift = params.typesShift,
                    selectedTypeId = data.id,
                ),
            ).let {
                addView(R.id.containerNotificationTypes, it)
            }
        }

        // Populate container with empty items to preserve prev next controls position
        fun addEmptyType() {
            getTypeControlView(
                icon = null,
                color = null,
                checkState = GoalCheckmarkView.CheckState.HIDDEN,
                isComplete = false,
                intent = null,
            ).let {
                addView(R.id.containerNotificationTypes, it)
            }
        }

        currentTypes.forEach {
            when (it) {
                is NotificationControlsParams.Type.Present -> addPresentType(it)
                is NotificationControlsParams.Type.Empty -> addEmptyType()
            }
        }

        // Next button
        setImageViewBitmap(
            R.id.ivNotificationTypesNext,
            getIconBitmap(params.controlIconNext, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT,
                requestCode = getRequestCode(from),
                from = from,
                recordTypesShift = (params.typesShift + TYPES_LIST_SIZE)
                    .takeUnless { it >= params.types.size }
                    ?: params.typesShift,
            ),
        )
    }

    private fun RemoteViews.addTagControls(
        from: From,
        params: NotificationControlsParams.Enabled,
    ) {
        // Prev button
        setImageViewBitmap(
            R.id.ivNotificationTagsPrev,
            getIconBitmap(params.controlIconPrev, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsPrev,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAGS_PREV,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                recordTypesShift = params.typesShift,
                recordTagsShift = (params.tagsShift - TAGS_LIST_SIZE)
                    .coerceAtLeast(0),
            ),
        )

        // Types buttons
        val currentTags = params.tags.drop(params.tagsShift).take(TAGS_LIST_SIZE)
        currentTags.forEach {
            getTagControlView(
                text = it.text,
                color = it.color,
                intent = getPendingSelfIntent(
                    context = context,
                    action = ACTION_NOTIFICATION_CONTROLS_TAG_CLICK,
                    requestCode = getRequestCode(
                        from = from,
                        additionalInfo = RequestCode.AdditionalInfo.TypeId(it.id),
                    ),
                    selectedTypeId = params.selectedTypeId,
                    from = from,
                    recordTagId = it.id,
                    recordTypesShift = params.typesShift,
                ),
            ).let {
                addView(R.id.containerNotificationTags, it)
            }
        }

        repeat(TAGS_LIST_SIZE - currentTags.size) {
            getTagControlView(
                text = "",
                color = null,
                intent = null,
            ).let {
                addView(R.id.containerNotificationTags, it)
            }
        }

        // Next button
        setImageViewBitmap(
            R.id.ivNotificationTagsNext,
            getIconBitmap(params.controlIconNext, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                recordTypesShift = params.typesShift,
                recordTagsShift = (params.tagsShift + TAGS_LIST_SIZE)
                    .takeUnless { it >= params.tags.size }
                    ?: params.tagsShift,
            ),
        )
    }

    private fun getTypeControlView(
        icon: RecordTypeIcon?,
        color: Int?,
        checkState: GoalCheckmarkView.CheckState,
        isComplete: Boolean,
        intent: PendingIntent?,
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notification_type_layout)
            .apply {
                if (icon != null && color != null) {
                    val bitmap = getIconBitmap(
                        icon = icon,
                        color = color,
                        checkState = checkState,
                        isComplete = isComplete,
                    )
                    setViewVisibility(R.id.containerNotificationType, View.VISIBLE)
                    setImageViewBitmap(R.id.ivNotificationType, bitmap)
                } else {
                    setViewVisibility(R.id.containerNotificationType, View.INVISIBLE)
                }
                if (intent != null) {
                    setOnClickPendingIntent(R.id.btnNotificationType, intent)
                }
            }
    }

    private fun getTagControlView(
        text: String,
        color: Int?,
        intent: PendingIntent?,
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notification_tag_layout)
            .apply {
                setTextViewText(R.id.tvNotificationTag, text)
                if (color != null) {
                    setViewVisibility(R.id.containerNotificationTag, View.VISIBLE)
                    setInt(R.id.ivNotificationTag, "setColorFilter", color)
                } else {
                    setViewVisibility(R.id.containerNotificationTag, View.INVISIBLE)
                }
                if (intent != null) {
                    setOnClickPendingIntent(R.id.btnNotificationTag, intent)
                }
            }
    }

    private fun getPendingSelfIntent(
        context: Context,
        action: String,
        requestCode: Int,
        from: From,
        selectedTypeId: Long? = null,
        recordTagId: Long? = null,
        recordTypesShift: Int? = null,
        recordTagsShift: Int? = null,
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = action
        notificationControlsMapper.mapFromToExtra(from).let { intent.putExtra(ARGS_CONTROLS_FROM, it) }
        (from as? From.ActivityNotification)?.recordTypeId?.let { intent.putExtra(ARGS_TYPE_ID, it) }
        selectedTypeId?.let { intent.putExtra(ARGS_SELECTED_TYPE_ID, it) }
        recordTagId?.let { intent.putExtra(ARGS_TAG_ID, it) }
        recordTypesShift.let { intent.putExtra(ARGS_TYPES_SHIFT, it) }
        recordTagsShift?.let { intent.putExtra(ARGS_TAGS_SHIFT, it) }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntents.getFlags(),
        )
    }

    private fun getIconBitmap(
        icon: RecordTypeIcon,
        color: Int,
        checkState: GoalCheckmarkView.CheckState = GoalCheckmarkView.CheckState.HIDDEN,
        isComplete: Boolean = false,
    ): Bitmap = synchronized(iconView) {
        return iconView.apply {
            itemIcon = icon
            itemColor = color
            itemCheckState = checkState
            itemIsComplete = isComplete
            measureExactly(iconSize)
        }.getBitmapFromView()
    }

    private fun getRequestCode(
        from: From,
        additionalInfo: RequestCode.AdditionalInfo = RequestCode.AdditionalInfo.Nothing,
    ): Int {
        return RequestCode(
            from = from,
            additionalInfo = additionalInfo,
        ).hashCode()
    }

    sealed interface From {
        data class ActivityNotification(val recordTypeId: Long) : From
        object ActivitySwitch : From
    }

    private data class RequestCode(
        val from: From,
        val additionalInfo: AdditionalInfo,
    ) {

        sealed interface AdditionalInfo {
            data class TypeId(val id: Long) : AdditionalInfo
            object Nothing : AdditionalInfo
        }
    }

    companion object {
        const val ACTION_NOTIFICATION_CONTROLS_STOP =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onStop"
        const val ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypeClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_CLICK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagClick"

        const val ACTION_NOTIFICATION_CONTROLS_TYPES_PREV =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypesPrevClick"
        const val ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypesNextClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAGS_PREV =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagsPrevClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagsNextClick"

        const val ARGS_CONTROLS_FROM = "controlsFrom"
        const val ARGS_TYPE_ID = "typeId"
        const val ARGS_SELECTED_TYPE_ID = "selectedTypeId"
        const val ARGS_TAG_ID = "tagId"
        const val ARGS_TYPES_SHIFT = "typesShift"
        const val ARGS_TAGS_SHIFT = "tagsShift"

        const val TYPES_LIST_SIZE = 6
        private const val TAGS_LIST_SIZE = 4
    }
}