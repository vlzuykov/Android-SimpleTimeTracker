package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.StatisticsViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.OneShotValue
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailPieChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionGraph
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionGraphViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailDataDistributionModeViewData
import javax.inject.Inject

class StatisticsDetailDataDistributionInteractor @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val statisticsViewDataMapper: StatisticsViewDataMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun mapDataDistribution(
        mode: DataDistributionMode,
        graph: DataDistributionGraph,
        records: List<RecordBase>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        if (records.isEmpty()) return emptyList()
        val result = mutableListOf<ViewHolderType>()

        val filterType = mapFilterType(
            mode = mode,
        )
        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = typesMap,
        )
        val statistics = getStatistics(
            mode = mode,
            allRecords = records,
        )
        val chart = mapChart(
            graph = graph,
            filterType = filterType,
            statistics = statistics,
            data = dataHolders,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
        )
        val items = mapItemsList(
            statistics = statistics,
            data = dataHolders,
            filterType = filterType,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )

        result += mapHint()
        result += chart
        result += mapModeControl(mode)
        result += mapGraphControl(graph)
        result += items

        return result
    }

    private suspend fun getStatistics(
        mode: DataDistributionMode,
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        return when (mode) {
            DataDistributionMode.ACTIVITY -> {
                statisticsInteractor.getActivityRecordsFull(
                    allRecords = allRecords,
                )
            }
            DataDistributionMode.CATEGORY -> {
                statisticsCategoryInteractor.getCategoryRecords(
                    allRecords = allRecords,
                    addUncategorized = true,
                )
            }
            DataDistributionMode.TAG -> {
                statisticsTagInteractor.getTagRecords(
                    allRecords = allRecords,
                    addUncategorized = true,
                )
            }
        }.let(statisticsInteractor::getStatisticsData)
    }

    private fun mapItemsList(
        shift: Int = 0,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        filterType: ChartFilterType,
        filteredIds: List<Long> = emptyList(),
        showDuration: Boolean = true,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsViewData> {
        return statisticsViewDataMapper.mapItemsList(
            shift = shift,
            statistics = statistics,
            data = data,
            filterType = filterType,
            filteredIds = filteredIds,
            showDuration = showDuration,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    private suspend fun mapChart(
        graph: DataDistributionGraph,
        filterType: ChartFilterType,
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
    ): ViewHolderType {
        return when (graph) {
            DataDistributionGraph.PIE_CHART -> {
                statisticsChartViewDataInteractor.getChart(
                    filterType = filterType,
                    filteredIds = emptyList(),
                    statistics = statistics,
                    dataHolders = data,
                    types = typesMap,
                    isDarkTheme = isDarkTheme,
                ).let {
                    StatisticsDetailPieChartViewData(
                        block = StatisticsDetailBlock.DataDistributionPieChart,
                        data = it,
                        animate = OneShotValue(true),
                    )
                }
            }
            DataDistributionGraph.BAR_CHART -> {
                mapBarChartData(
                    statistics = statistics,
                    data = data,
                    isDarkTheme = isDarkTheme,
                ).let {
                    StatisticsDetailBarChartViewData(
                        block = StatisticsDetailBlock.DataDistributionBarChart,
                        singleColor = null,
                        marginTopDp = 0,
                        data = it,
                    )
                }
            }
        }
    }

    private fun mapBarChartData(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        isDarkTheme: Boolean,
    ): StatisticsDetailChartViewData {
        val chartData = statistics
            .mapNotNull { statistic ->
                val chart = mapBarChartItem(
                    statistics = statistic,
                    dataHolder = data[statistic.id],
                    isDarkTheme = isDarkTheme,
                ) ?: return@mapNotNull null
                chart to statistic.data.duration
            }
            .sortedBy { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }

        val (legendSuffix, isMinutes) = statisticsDetailViewDataMapper.mapLegendSuffix(chartData)

        return StatisticsDetailChartViewData(
            visible = true,
            data = chartData.map {
                val value = it.durations.map { (duration, color) ->
                    statisticsDetailViewDataMapper.formatInterval(duration, isMinutes) to color
                }
                BarChartView.ViewData(
                    value = value,
                    legend = it.legend,
                )
            },
            legendSuffix = legendSuffix,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = false,
            showSelectedBarOnStart = false,
            goalValue = 0f,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    private fun mapBarChartItem(
        statistics: Statistics,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
    ): ChartBarDataDuration? {
        return when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                ChartBarDataDuration(
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            colorMapper.toUntrackedColor(isDarkTheme),
                    ),
                )
            }
            statistics.id == UNCATEGORIZED_ITEM_ID -> {
                ChartBarDataDuration(
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            colorMapper.toUntrackedColor(isDarkTheme),
                    ),
                )
            }
            dataHolder != null -> {
                ChartBarDataDuration(
                    legend = "",
                    durations = listOf(
                        statistics.data.duration to
                            dataHolder.color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    ),
                )
            }
            else -> {
                null
            }
        }
    }

    private fun mapHint(): ViewHolderType {
        return StatisticsDetailHintViewData(
            block = StatisticsDetailBlock.DataDistributionHint,
            text = resourceRepo.getString(R.string.statistics_detail_data_split_hint),
        )
    }

    private fun mapModeControl(
        mode: DataDistributionMode,
    ): ViewHolderType {
        val values = listOf(
            DataDistributionMode.ACTIVITY,
            DataDistributionMode.CATEGORY,
            DataDistributionMode.TAG,
        )

        return StatisticsDetailButtonsRowViewData(
            block = StatisticsDetailBlock.DataDistributionMode,
            marginTopDp = 4,
            data = values.map {
                StatisticsDetailDataDistributionModeViewData(
                    mode = it,
                    name = when (it) {
                        DataDistributionMode.ACTIVITY -> R.string.activity_hint
                        DataDistributionMode.CATEGORY -> R.string.category_hint
                        DataDistributionMode.TAG -> R.string.record_tag_hint_short
                    }.let(resourceRepo::getString),
                    isSelected = it == mode,
                    textSizeSp = 12,
                )
            },
        )
    }

    private fun mapGraphControl(
        graph: DataDistributionGraph,
    ): ViewHolderType {
        val values = listOf(
            DataDistributionGraph.PIE_CHART,
            DataDistributionGraph.BAR_CHART,
        )
        return StatisticsDetailButtonsRowViewData(
            block = StatisticsDetailBlock.DataDistributionGraph,
            marginTopDp = -10,
            data = values.map {
                StatisticsDetailDataDistributionGraphViewData(
                    graph = it,
                    name = when (it) {
                        DataDistributionGraph.PIE_CHART ->
                            R.string.statistics_detail_data_split_pie_chart
                        DataDistributionGraph.BAR_CHART ->
                            R.string.statistics_detail_data_split_bar_chart
                    }.let(resourceRepo::getString),
                    isSelected = it == graph,
                )
            },
        )
    }

    private fun mapFilterType(mode: DataDistributionMode): ChartFilterType {
        return when (mode) {
            DataDistributionMode.ACTIVITY -> ChartFilterType.ACTIVITY
            DataDistributionMode.CATEGORY -> ChartFilterType.CATEGORY
            DataDistributionMode.TAG -> ChartFilterType.RECORD_TAG
        }
    }
}