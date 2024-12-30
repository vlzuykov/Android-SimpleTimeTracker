package com.example.util.simpletimetracker.feature_suggestions.adapter

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionsButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_suggestions.databinding.ItemActivitySuggestionsButtonBinding as Binding

// TODO SUG refactor with record quick actions button, and complex rules button.
// TODO SUG remove ripple from icon background if background is transparent.
// TODO SUG change button background color to appInactive color.
fun createActivitySuggestionsButtonAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvActivitySuggestionsButton.text = item.text
        ivActivitySuggestionsButton.setImageResource(item.icon)
        ivActivitySuggestionsButton.imageTintList = ColorStateList.valueOf(item.iconColor)
        cardActivitySuggestionsButton.setCardBackgroundColor(item.iconBackgroundColor)
        itemActivitySuggestionsButton.isEnabled = item.isEnabled
        itemActivitySuggestionsButton.setOnClickWith(item, onClick)
    }
}

data class ActivitySuggestionsButtonViewData(
    val block: Block,
    val text: String,
    @DrawableRes val icon: Int,
    @ColorInt val iconColor: Int,
    @ColorInt val iconBackgroundColor: Int,
    val isEnabled: Boolean,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    enum class Block {
        ADD,
        CALCULATE,
    }
}