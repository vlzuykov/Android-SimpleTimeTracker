package com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class StatisticsGoalViewData(
    val id: Long,
    val name: String,
    @ColorInt val color: Int,
    val icon: RecordTypeIcon?,
    val goal: Goal,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is StatisticsGoalViewData

    data class Goal(
        val goalCurrent: String,
        val goal: String,
        val goalPercent: String,
        val goalState: GoalCheckmarkView.CheckState,
        val percent: Long,
    )
}