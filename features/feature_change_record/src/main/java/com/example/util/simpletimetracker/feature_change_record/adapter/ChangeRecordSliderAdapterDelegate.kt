package com.example.util.simpletimetracker.feature_change_record.adapter

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordSliderViewData as ViewData
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordSliderItemBinding as Binding

fun createChangeRecordSliderAdapterDelegate(
    onValueChanged: (ViewData, Float) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData
        // To avoid illegal state exception.
        val actualMax = item.max.coerceAtLeast(item.min + 1)

        root.tag = item.block
        if (item.min != sliderChangeRecordItem.valueFrom) {
            sliderChangeRecordItem.valueFrom = item.min
        }
        if (item.max != sliderChangeRecordItem.valueTo) {
            sliderChangeRecordItem.valueTo = actualMax
        }
        if (item.value != sliderChangeRecordItem.value) {
            sliderChangeRecordItem.value = item.value
        }
        sliderChangeRecordItem.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onValueChanged(item, value)
        }
    }
}

data class ChangeRecordSliderViewData(
    val block: ChangeRecordActionsBlock,
    val min: Float,
    val max: Float,
    val value: Float,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData
}