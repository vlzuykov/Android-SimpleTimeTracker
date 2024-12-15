package com.example.util.simpletimetracker.feature_change_goals.viewData

import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

data class ChangeRecordTypeGoalsState(
    val session: GoalState,
    val daily: GoalState,
    val weekly: GoalState,
    val monthly: GoalState,
    val daysOfWeek: List<DayOfWeek>,
) {

    data class GoalState(
        val type: RecordTypeGoal.Type,
        val subtype: RecordTypeGoal.Subtype,
    )
}