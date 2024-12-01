package com.example.util.simpletimetracker.core.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.model.ChartFilterType

data class ChartFilterTypeViewData(
    val filterType: ChartFilterType,
    override val name: String,
    override val isSelected: Boolean,
    override val textSizeSp: Int?,
) : ButtonsRowViewData() {

    override val id: Long = filterType.ordinal.toLong()
}