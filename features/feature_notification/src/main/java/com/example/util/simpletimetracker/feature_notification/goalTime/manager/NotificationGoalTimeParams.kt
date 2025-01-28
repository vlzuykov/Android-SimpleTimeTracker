package com.example.util.simpletimetracker.feature_notification.goalTime.manager

import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class NotificationGoalTimeParams(
    val idData: RecordTypeGoal.IdData,
    val goalRange: RecordTypeGoal.Range,
    val icon: RecordTypeIcon,
    val color: Int,
    val text: String,
    val description: String,
    val checkState: GoalCheckmarkView.CheckState,
)