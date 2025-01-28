package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

sealed interface NotificationControlsParams {
    data object Disabled : NotificationControlsParams

    data class Enabled(
        val hint: String,
        val types: List<Type>,
        val typesShift: Int,
        val tags: List<Tag>,
        val tagsShift: Int,
        val controlIconPrev: RecordTypeIcon,
        val controlIconNext: RecordTypeIcon,
        val controlIconColor: Int,
        val filteredTypeColor: Int,
        val selectedTypeId: Long?,
    ) : NotificationControlsParams

    sealed interface Type {
        data class Present(
            val id: Long,
            val icon: RecordTypeIcon,
            val color: Int,
            val checkState: GoalCheckmarkView.CheckState,
            val isComplete: Boolean,
        ) : Type

        data object Empty : Type
    }

    data class Tag(
        val id: Long,
        val text: String,
        val color: Int,
    )
}