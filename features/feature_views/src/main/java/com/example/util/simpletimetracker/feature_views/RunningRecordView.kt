package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_views.databinding.RecordRunningViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.setForegroundSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RunningRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = RecordRunningViewLayoutBinding.inflate(layoutInflater, this)

    var itemName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemTagName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemColor: Int = 0
        set(value) {
            field = value
            setCardBackgroundColor(value)
            setStripesColor(value)
            setNowIconColor(value)
        }

    var itemTagColor: Int = Color.WHITE
        set(value) {
            field = value
            setItemName()
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivRunningRecordItemIcon.itemIcon = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            binding.tvRunningRecordItemTimeStarted.text = value
            field = value
        }

    var itemTimer: String = ""
        set(value) {
            binding.tvRunningRecordItemTimer.text = value
            field = value
        }

    var itemTimerTotal: String = ""
        set(value) {
            binding.tvRunningRecordItemTimerTotal.text = value
            binding.tvRunningRecordItemTimerTotal.visible = value.isNotEmpty()
            field = value
        }

    var itemGoalTime: String = ""
        set(value) {
            binding.tvRunningRecordItemGoalTime.text = value
            binding.tvRunningRecordItemGoalTime.visible = value.isNotEmpty()
            field = value
        }

    var itemGoalTimeCheck: GoalCheckmarkView.CheckState = GoalCheckmarkView.CheckState.HIDDEN
        set(value) {
            binding.ivRunningRecordItemGoalTimeCheck.itemCheckState = value
            field = value
        }

    var itemComment: String = ""
        set(value) {
            binding.tvRunningRecordItemComment.text = value
            binding.tvRunningRecordItemComment.visible = value.isNotEmpty()
            field = value
        }

    var itemNowIconVisible: Boolean = false
        set(value) {
            binding.tvRunningRecordItemNow.visible = value
            field = value
        }

    init {
        initProps()
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initProps() {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.RunningRecordView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RunningRecordView_itemName)) {
                    itemName = getString(R.styleable.RunningRecordView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemColor)) {
                    itemColor = getColor(R.styleable.RunningRecordView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.RunningRecordView_itemTagName)) {
                    itemTagName = getString(R.styleable.RunningRecordView_itemTagName).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTagColor)) {
                    itemTagColor = getColor(R.styleable.RunningRecordView_itemTagColor, Color.WHITE)
                }

                if (hasValue(R.styleable.RunningRecordView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.RunningRecordView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.RunningRecordView_itemIconText)) {
                    itemIcon = getString(R.styleable.RunningRecordView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimeStarted)) {
                    itemTimeStarted = getString(R.styleable.RunningRecordView_itemTimeStarted).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimer)) {
                    itemTimer = getString(R.styleable.RunningRecordView_itemTimer).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemTimerDay)) {
                    itemTimerTotal = getString(R.styleable.RunningRecordView_itemTimerDay).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemGoalTime)) {
                    itemGoalTime = getString(R.styleable.RunningRecordView_itemGoalTime).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemComment)) {
                    itemComment = getString(R.styleable.RunningRecordView_itemComment).orEmpty()
                }

                if (hasValue(R.styleable.RunningRecordView_itemNowIconVisible)) {
                    itemNowIconVisible = getBoolean(R.styleable.RunningRecordView_itemNowIconVisible, false)
                }

                recycle()
            }
    }

    private fun setItemName() = with(binding) {
        if (itemTagName.isEmpty()) {
            tvRunningRecordItemName.text = itemName
        } else {
            val name = "$itemName - $itemTagName"
            tvRunningRecordItemName.text = name.toSpannableString().setForegroundSpan(
                color = itemTagColor,
                start = itemName.length,
                length = name.length - itemName.length,
            )
        }
    }

    private fun setStripesColor(@ColorInt value: Int) {
        ColorUtils.normalizeLightness(value, factor = 0.03f)
            .also(binding.viewRecordItemStripeStart::setBackgroundColor)
            .also(binding.viewRecordItemStripeEnd::setBackgroundColor)
    }

    private fun setNowIconColor(@ColorInt value: Int) {
        ColorUtils.darkenColor(value)
            .let(ColorStateList::valueOf)
            .let { ViewCompat.setBackgroundTintList(binding.tvRunningRecordItemNow, it) }
    }
}