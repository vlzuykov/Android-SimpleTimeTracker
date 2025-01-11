package com.example.util.simpletimetracker.feature_base_adapter.button

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemButtonLayoutBinding as Binding

// TODO remove ripple from icon background if background is transparent.
// TODO SUG add backup tests
// TODO GOAL add backup tests, raise test file version
fun createButtonAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.setMargins(
            start = item.marginHorizontalDp,
            end = item.marginHorizontalDp,
        )
        tvButton.text = item.text
        when (item.icon) {
            is ViewData.Icon.Hidden -> {
                cardButton.isVisible = false
            }
            is ViewData.Icon.Present -> {
                cardButton.isVisible = true
                ivButton.setImageResource(item.icon.icon)
                ivButton.imageTintList = ColorStateList.valueOf(item.icon.iconColor)
                cardButton.setCardBackgroundColor(item.icon.iconBackgroundColor)
            }
        }
        root.setCardBackgroundColor(item.backgroundColor)
        itemButton.isEnabled = item.isEnabled
        itemButton.setOnClickWith(item, onClick)
    }
}

data class ButtonViewData(
    val id: Id,
    val text: String,
    val icon: Icon,
    @ColorInt val backgroundColor: Int,
    val isEnabled: Boolean,
    val marginHorizontalDp: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean =
        other is ViewData

    interface Id

    sealed interface Icon {
        data object Hidden : Icon
        data class Present(
            @DrawableRes val icon: Int,
            @ColorInt val iconColor: Int,
            @ColorInt val iconBackgroundColor: Int,
        ) : Icon
    }
}