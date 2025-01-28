package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_views.databinding.IconViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class IconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = IconViewLayoutBinding.inflate(layoutInflater, this)

    init {
        context
            .obtainStyledAttributes(attrs, R.styleable.IconView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.IconView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.IconView_itemIcon, R.drawable.unknown).let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.IconView_itemIconText)) {
                    itemIcon = getString(R.styleable.IconView_itemIconText).orEmpty().let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.IconView_itemIconColor)) {
                    itemIconColor = getColor(R.styleable.IconView_itemIconColor, Color.WHITE)
                }

                if (hasValue(R.styleable.IconView_itemIconAlpha)) {
                    itemIconAlpha = getFloat(R.styleable.IconView_itemIconAlpha, 1.0f)
                }

                recycle()
            }
    }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            when (value) {
                is RecordTypeIcon.Image -> setImageIcon(value.iconId)
                is RecordTypeIcon.Text -> setTextIcon(value.text)
            }
            field = value
        }

    var itemIconColor: Int = 0
        set(value) {
            ViewCompat.setBackgroundTintList(binding.ivIconViewImage, ColorStateList.valueOf(value))
            binding.tvIconViewEmoji.setTextColor(value)
            field = value
        }

    var itemIconAlpha: Float = 1.0f
        set(value) {
            binding.ivIconViewImage.alpha = value
            binding.tvIconViewEmoji.alpha = value
            field = value
        }

    private fun setImageIcon(value: Int) = with(binding) {
        ivIconViewImage.setBackgroundResource(value)
        ivIconViewImage.tag = value

        ivIconViewImage.visible = true
        tvIconViewEmoji.visible = false
    }

    private fun setTextIcon(value: String) = with(binding) {
        tvIconViewEmoji.text = value

        tvIconViewEmoji.visible = true
        ivIconViewImage.visible = false
    }
}