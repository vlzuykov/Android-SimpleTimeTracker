package com.example.util.simpletimetracker.feature_change_goals.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal

data class ChangeRecordTypeGoalSubtypeViewData(
    val subtype: RecordTypeGoal.Subtype,
    override val name: String,
    override val isSelected: Boolean,
    override val textSizeSp: Int?,
) : ButtonsRowViewData() {

    override val id: Long = subtype::class.java.simpleName.hashCode().toLong()
}