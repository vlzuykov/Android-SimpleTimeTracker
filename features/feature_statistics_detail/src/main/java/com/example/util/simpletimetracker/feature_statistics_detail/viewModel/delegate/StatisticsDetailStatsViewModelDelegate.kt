package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailStatsInteractor
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionGraph
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionModeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionGraphViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailStatsViewModelDelegate @Inject constructor(
    private val statsInteractor: StatisticsDetailStatsInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<StatisticsDetailStatsViewData?> by lazySuspend {
        loadEmptyViewData().also { parent?.updateContent() }
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null
    private var dataDistributionMode = DataDistributionMode.ACTIVITY
    private var dataDistributionGraph = DataDistributionGraph.PIE_CHART

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun onDataDistributionModeClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailDataDistributionModeViewData) return
        this.dataDistributionMode = viewData.mode
        updateViewData()
    }

    fun onDataDistributionGraphClick(viewData: ButtonsRowViewData) {
        if (viewData !is StatisticsDetailDataDistributionGraphViewData) return
        this.dataDistributionGraph = viewData.graph
        updateViewData()
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
    }

    private fun loadEmptyViewData(): StatisticsDetailStatsViewData {
        return statsInteractor.getEmptyStatsViewData()
    }

    private suspend fun loadViewData(): StatisticsDetailStatsViewData? {
        val parent = parent ?: return null

        return statsInteractor.getStatsViewData(
            records = parent.records,
            compareRecords = parent.compareRecords,
            showComparison = parent.comparisonFilter.isNotEmpty(),
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
            dataDistributionMode = dataDistributionMode,
            dataDistributionGraph = dataDistributionGraph,
        )
    }
}