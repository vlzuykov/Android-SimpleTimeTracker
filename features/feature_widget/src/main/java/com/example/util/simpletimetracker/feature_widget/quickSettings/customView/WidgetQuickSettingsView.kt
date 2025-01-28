package com.example.util.simpletimetracker.feature_widget.quickSettings.customView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetQuickSettingsViewLayoutBinding

class WidgetQuickSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = WidgetQuickSettingsViewLayoutBinding.inflate(layoutInflater, this)

    init {
        ContextCompat.getColor(context, R.color.widget_universal_background_color).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
        cardElevation = 0f
        preventCornerOverlap = false
        useCompatPadding = false

        context.obtainStyledAttributes(attrs, R.styleable.WidgetQuickSettingsView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.WidgetQuickSettingsView_itemName)) {
                    itemName = getString(R.styleable.WidgetQuickSettingsView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.WidgetQuickSettingsView_itemIcon)) {
                    val data = getResourceId(R.styleable.WidgetQuickSettingsView_itemIcon, R.drawable.unknown)
                    itemIcon = RecordTypeIcon.Image(data)
                }

                if (hasValue(R.styleable.WidgetQuickSettingsView_itemIconColor)) {
                    itemIconColor = getColor(R.styleable.WidgetQuickSettingsView_itemIconColor, Color.WHITE)
                }

                if (hasValue(R.styleable.WidgetQuickSettingsView_itemBackgroundAlpha)) {
                    itemBackgroundAlpha = getFloat(R.styleable.WidgetQuickSettingsView_itemBackgroundAlpha, 0.5f)
                }

                recycle()
            }
    }

    var itemName: String = ""
        set(value) {
            binding.tvWidgetQuickSettingsName.text = value
            field = value
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivWidgetQuickSettingsIcon.itemIcon = value
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            binding.ivWidgetQuickSettingsIcon.itemIconColor = value
            field = value
        }

    var itemBackgroundAlpha: Float = 0.5f
        set(value) {
            ColorUtils.changeAlpha(
                color = ContextCompat.getColor(context, R.color.widget_universal_background_color),
                alpha = value,
            ).let(::setCardBackgroundColor)
            field = value
        }
}