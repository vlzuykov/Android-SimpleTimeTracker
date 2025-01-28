package com.example.util.simpletimetracker.feature_statistics_detail.customView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.createStatisticsPreviewCompareAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailPreviewsViewLayoutBinding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class StatisticsDetailPreviewsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createStatisticsPreviewCompareAdapterDelegate(),
            createStatisticsPreviewAdapterDelegate(),
        )
    }

    private val binding = StatisticsDetailPreviewsViewLayoutBinding.inflate(layoutInflater, this)

    init {
        initRecycler()
        initEditMode()
    }

    private fun initRecycler() {
        binding.rvStatisticsDetailPreviewsContainer.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@StatisticsDetailPreviewsView.adapter
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            List(3) {
                StatisticsDetailPreviewViewData(
                    id = it.toLong(),
                    type = StatisticsDetailPreviewViewData.Type.FILTER,
                    name = it.toString(),
                    iconId = null,
                    color = Color.BLACK,
                )
            }.let(adapter::replace)
        }
    }
}