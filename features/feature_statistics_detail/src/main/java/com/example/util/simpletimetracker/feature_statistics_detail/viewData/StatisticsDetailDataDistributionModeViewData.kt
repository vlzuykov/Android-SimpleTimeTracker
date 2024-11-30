package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode

data class StatisticsDetailDataDistributionModeViewData(
    val mode: DataDistributionMode,
    override val name: String,
    override val isSelected: Boolean,
) : ButtonsRowViewData() {

    override val id: Long = mode.ordinal.toLong()
}