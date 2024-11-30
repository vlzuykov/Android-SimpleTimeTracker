package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.interactor.StatisticsChartViewDataInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsMediator
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.StatisticsDataHolder
import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.StatisticsCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.StatisticsTagInteractor
import com.example.util.simpletimetracker.domain.mapper.StatisticsMapper
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.OneShotValue
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.Statistics
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.statisticsTag.StatisticsTagViewData
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
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class StatisticsDetailDataDistributionInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val statisticsMapper: StatisticsMapper,
    private val statisticsTagInteractor: StatisticsTagInteractor,
    private val statisticsCategoryInteractor: StatisticsCategoryInteractor,
    private val statisticsMediator: StatisticsMediator,
    private val statisticsChartViewDataInteractor: StatisticsChartViewDataInteractor,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    // TODO STATS add translations
    // TODO STATS refactor StatisticsTagViewData
    suspend fun mapDataDistribution(
        mode: DataDistributionMode,
        graph: DataDistributionGraph,
        records: List<RecordBase>,
        typesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        val filterType = when (mode) {
            DataDistributionMode.ACTIVITY -> ChartFilterType.ACTIVITY
            DataDistributionMode.CATEGORY -> ChartFilterType.CATEGORY
            DataDistributionMode.TAG -> ChartFilterType.RECORD_TAG
        }
        val dataHolders = statisticsMediator.getDataHolders(
            filterType = filterType,
            types = typesMap,
        )
        val statistics = getStatistics(
            mode = mode,
            allRecords = records,
        )

        result += mapHint()
        result += mapChart(
            graph = graph,
            filterType = filterType,
            statistics = statistics,
            data = dataHolders,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
        )
        result += mapModeControl(mode)
        result += mapGraphControl(graph)
        result += mapItemsList(
            statistics = statistics,
            data = dataHolders,
            filterType = filterType,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )

        return result
    }

    private suspend fun getStatistics(
        mode: DataDistributionMode,
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        return when (mode) {
            DataDistributionMode.ACTIVITY ->
                getActivityStatistics(allRecords)
            DataDistributionMode.CATEGORY ->
                getCategoryStatistics(allRecords)
            DataDistributionMode.TAG ->
                getTagStatistics(allRecords)
        }
    }

    private fun getActivityStatistics(
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        val activities: MutableMap<Long, MutableList<RecordBase>> = mutableMapOf()

        allRecords.forEach { record ->
            record.typeIds.forEach { typeId ->
                activities.getOrPut(typeId) { mutableListOf() }.add(record)
            }
        }

        return activities.let(::getStatisticsData)
    }

    private suspend fun getCategoryStatistics(
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        return statisticsCategoryInteractor.getCategoryRecords(
            allRecords = allRecords,
            addUncategorized = true,
        ).let(::getStatisticsData)
    }

    private fun getTagStatistics(
        allRecords: List<RecordBase>,
    ): List<Statistics> {
        return statisticsTagInteractor.getTagRecords(
            allRecords = allRecords,
            addUncategorized = true,
        ).let(::getStatisticsData)
    }

    private fun getStatisticsData(
        allRecords: Map<Long, List<RecordBase>>,
    ): List<Statistics> {
        return allRecords.map { (id, records) ->
            Statistics(
                id = id,
                Statistics.Data(
                    duration = records.sumOf(RecordBase::duration),
                    count = records.size.toLong(),
                ),
            )
        }
    }

    private fun mapItemsList(
        statistics: List<Statistics>,
        data: Map<Long, StatisticsDataHolder>,
        filterType: ChartFilterType,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<ViewHolderType> {
        val sumDuration = statistics.sumOf { it.data.duration }
        val statisticsSize = statistics.size

        return statistics
            .mapNotNull { statistic ->
                val item = mapItem(
                    filterType = filterType,
                    statistics = statistic,
                    sumDuration = sumDuration,
                    dataHolder = data[statistic.id],
                    isDarkTheme = isDarkTheme,
                    statisticsSize = statisticsSize,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ) ?: return@mapNotNull null
                item to statistic.data.duration
            }
            .sortedByDescending { (_, duration) -> duration }
            .map { (statistics, _) -> statistics }
    }

    // TODO STATS refactor duplication
    private fun mapItem(
        filterType: ChartFilterType,
        statistics: Statistics,
        sumDuration: Long,
        dataHolder: StatisticsDataHolder?,
        isDarkTheme: Boolean,
        statisticsSize: Int,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): ViewHolderType? {
        val durationPercent = statisticsMapper.getDurationPercentString(
            sumDuration = sumDuration,
            duration = statistics.data.duration,
            statisticsSize = statisticsSize,
        )
        val duration = timeMapper.formatInterval(
            interval = statistics.data.duration,
            forceSeconds = showSeconds,
            useProportionalMinutes = useProportionalMinutes,
        )

        return when {
            statistics.id == UNTRACKED_ITEM_ID -> {
                StatisticsTagViewData(
                    id = statistics.id,
                    name = R.string.untracked_time_name
                        .let(resourceRepo::getString),
                    duration = duration,
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.unknown),
                    color = colorMapper.toUntrackedColor(isDarkTheme),
                )
            }
            statistics.id == UNCATEGORIZED_ITEM_ID -> {
                StatisticsTagViewData(
                    id = statistics.id,
                    name = if (filterType == ChartFilterType.RECORD_TAG) {
                        R.string.change_record_untagged
                    } else {
                        R.string.uncategorized_time_name
                    }.let(resourceRepo::getString),
                    duration = duration,
                    percent = durationPercent,
                    icon = RecordTypeIcon.Image(R.drawable.unknown),
                    color = colorMapper.toUntrackedColor(isDarkTheme),
                )
            }
            dataHolder != null -> {
                StatisticsTagViewData(
                    id = statistics.id,
                    name = dataHolder.name,
                    duration = duration,
                    percent = durationPercent,
                    icon = dataHolder.icon
                        ?.let(iconMapper::mapIcon),
                    color = dataHolder.color
                        .let { colorMapper.mapToColorInt(it, isDarkTheme) },
                )
            }
            else -> null
        }
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
        return StatisticsDetailButtonsRowViewData(
            block = StatisticsDetailBlock.DataDistributionMode,
            marginTopDp = 4,
            data = DataDistributionMode.entries.map {
                StatisticsDetailDataDistributionModeViewData(
                    mode = it,
                    name = when (it) {
                        DataDistributionMode.ACTIVITY -> R.string.activity_hint
                        DataDistributionMode.CATEGORY -> R.string.category_hint
                        DataDistributionMode.TAG -> R.string.record_tag_hint_short
                    }.let(resourceRepo::getString),
                    isSelected = it == mode,
                )
            },
        )
    }

    private fun mapGraphControl(
        graph: DataDistributionGraph,
    ): ViewHolderType {
        return StatisticsDetailButtonsRowViewData(
            block = StatisticsDetailBlock.DataDistributionGraph,
            marginTopDp = -10,
            data = DataDistributionGraph.entries.map {
                StatisticsDetailDataDistributionGraphViewData(
                    graph = it,
                    name = when (it) {
                        DataDistributionGraph.PIE_CHART -> "Pie chart" // TODO STATS
                        DataDistributionGraph.BAR_CHART -> "Bar chart"
                    },
                    isSelected = it == graph,
                )
            },
        )
    }
}