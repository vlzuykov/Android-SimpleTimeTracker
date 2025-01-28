package com.example.util.simpletimetracker.core.view.buttonsRowView

import android.graphics.Color
import android.util.TypedValue
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.core.databinding.ButtonsRowItemLayoutBinding as Binding
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData as ViewData

fun createButtonsRowViewAdapterDelegate(
    selectedColor: Int,
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        val color = if (item.isSelected) selectedColor else Color.TRANSPARENT

        btnButtonsRowView.text = item.name
        btnButtonsRowView.setBackgroundColor(color)
        btnButtonsRowView.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            item.textSizeSp?.toFloat() ?: 14f,
        )
        btnButtonsRowView.setOnClickWith(item, onItemClick)
    }
}