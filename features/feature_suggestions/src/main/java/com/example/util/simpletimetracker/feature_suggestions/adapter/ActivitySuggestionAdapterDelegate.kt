package com.example.util.simpletimetracker.feature_suggestions.adapter

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionViewData as ViewData
import com.example.util.simpletimetracker.feature_suggestions.databinding.ItemActivitySuggestionLayoutBinding as Binding

fun createActivitySuggestionAdapterDelegate() = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding.viewActivitySuggestionItem) {
        item as ViewData

        itemColor = item.color
        itemIcon = item.iconId
        itemIconColor = item.iconColor
        itemName = item.name
    }
}

data class ActivitySuggestionViewData(
    val id: Id,
    val name: String,
    val iconId: RecordTypeIcon,
    @ColorInt val iconColor: Int,
    @ColorInt val color: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData

    data class Id(
        val suggestionTypeId: Long,
        val forTypeId: Long,
    )
}