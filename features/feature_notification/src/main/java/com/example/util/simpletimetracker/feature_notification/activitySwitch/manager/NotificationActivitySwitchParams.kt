package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationActivitySwitchParams(
    val icon: RecordTypeIcon,
    val color: Int,
    val title: String,
    val subtitle: String,
    val isDarkTheme: Boolean,
    val untrackedStartedTimeStamp: Long?,
    val prevRecordDuration: Long?,
    val controls: NotificationControlsParams,
)
