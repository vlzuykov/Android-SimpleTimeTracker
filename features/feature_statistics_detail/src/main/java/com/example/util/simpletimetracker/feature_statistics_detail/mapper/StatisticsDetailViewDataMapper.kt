package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.MULTITASK_ITEM_ID
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.rotateLeft
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.OneShotValue
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBarChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailBlock
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailButtonsRowViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailCardViewData
import com.example.util.simpletimetracker.feature_statistics_detail.adapter.StatisticsDetailHintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.customView.BarChartView
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataDuration
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartBarDataRange
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartLength
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.ChartSplitSortMode
import com.example.util.simpletimetracker.feature_statistics_detail.model.SplitChartGrouping
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartCompositeViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartLengthViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailChartViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailGroupingViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailPreviewViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailSplitGroupingViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToLong

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val resourceRepo: ResourceRepo,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
) {

    fun mapToPreview(
        recordType: RecordType,
        isDarkTheme: Boolean,
        isFirst: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = recordType.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = recordType.name.takeIf { isFirst }.orEmpty(),
            iconId = recordType.icon
                .let(iconMapper::mapIcon),
            color = recordType.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToCategorizedPreview(
        category: Category,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = category.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = category.name,
            iconId = null,
            color = category.color
                .let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToUncategorizedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val item = categoryViewDataMapper.mapToUncategorizedItem(
            isDarkTheme = isDarkTheme,
            isFiltered = false,
        )

        return StatisticsDetailPreviewViewData(
            id = item.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = item.name,
            iconId = RecordTypeIcon.Image(R.drawable.untagged),
            color = item.color,
        )
    }

    fun mapToTaggedPreview(
        tag: RecordTag,
        types: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val icon = recordTagViewDataMapper.mapIcon(tag, types)
        val color = recordTagViewDataMapper.mapColor(tag, types)

        return StatisticsDetailPreviewViewData(
            id = tag.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = tag.name,
            iconId = icon?.let(iconMapper::mapIcon),
            color = color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
        )
    }

    fun mapToUntaggedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        val item = categoryViewDataMapper.mapToUntaggedItem(
            isDarkTheme = isDarkTheme,
            isFiltered = false,
        )

        return StatisticsDetailPreviewViewData(
            id = item.id,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = item.name,
            iconId = item.icon,
            color = item.color,
        )
    }

    fun mapUntrackedPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = UNTRACKED_ITEM_ID,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapMultitaskPreview(
        isDarkTheme: Boolean,
        isForComparison: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = MULTITASK_ITEM_ID,
            type = if (isForComparison) {
                StatisticsDetailPreviewViewData.Type.COMPARISON
            } else {
                StatisticsDetailPreviewViewData.Type.FILTER
            },
            name = resourceRepo.getString(R.string.multitask_time_name),
            iconId = RecordTypeIcon.Image(R.drawable.multitask),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapToPreviewEmpty(
        isDarkTheme: Boolean,
    ): StatisticsDetailPreviewViewData {
        return StatisticsDetailPreviewViewData(
            id = 0,
            type = StatisticsDetailPreviewViewData.Type.FILTER,
            name = "",
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapToChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        splitByActivity: Boolean,
        canSplitByActivity: Boolean,
        canComparisonSplitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
        goalValue: Long,
        compareData: List<ChartBarDataDuration>,
        compareGoalValue: Long,
        showComparison: Boolean,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): StatisticsDetailChartCompositeViewData {
        val chartIsSplitByActivity = splitByActivity && canSplitByActivity
        val chartComparisonIsSplitByActivity = splitByActivity && canComparisonSplitByActivity

        val chartData = mapChartData(
            data = data,
            goal = goalValue,
            rangeLength = rangeLength,
            chartMode = chartMode,
            showSelectedBarOnStart = true,
            useSingleColor = !chartIsSplitByActivity,
            drawRoundCaps = !chartIsSplitByActivity,
        )
        val compareChartData = mapChartData(
            data = compareData,
            goal = compareGoalValue,
            rangeLength = rangeLength,
            chartMode = chartMode,
            showSelectedBarOnStart = false,
            useSingleColor = !chartComparisonIsSplitByActivity,
            drawRoundCaps = !chartComparisonIsSplitByActivity,
        )
        val (title, rangeAverages) = getRangeAverages(
            data = data,
            prevData = prevData,
            compareData = compareData,
            showComparison = showComparison,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )
        val splitByActivityItems = if (canSplitByActivity || canComparisonSplitByActivity) {
            mapSplitByActivityItems(
                splitByActivity = splitByActivity,
                splitSortMode = splitSortMode,
                isDarkTheme = isDarkTheme,
            )
        } else {
            emptyList()
        }
        val additionalChartButtonItems = mutableListOf<ViewHolderType>()
        additionalChartButtonItems += splitByActivityItems

        return StatisticsDetailChartCompositeViewData(
            chartData = chartData,
            compareChartData = compareChartData,
            showComparison = showComparison,
            rangeAveragesTitle = title,
            rangeAverages = rangeAverages,
            appliedChartGrouping = appliedChartGrouping,
            chartGroupingViewData = chartGroupingViewData,
            chartGroupingVisible = chartGroupingViewData.size > 1,
            appliedChartLength = appliedChartLength,
            chartLengthViewData = chartLengthViewData,
            chartLengthVisible = chartLengthViewData.isNotEmpty(),
            additionalChartButtonItems = additionalChartButtonItems,
        )
    }

    fun mapToEmptyChartViewData(
        ranges: List<ChartBarDataRange>,
        availableChartGroupings: List<ChartGrouping>,
        availableChartLengths: List<ChartLength>,
    ): StatisticsDetailChartCompositeViewData {
        val emptyChart = StatisticsDetailChartViewData(
            visible = false,
            data = emptyList(),
            legendSuffix = "",
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = false,
            showSelectedBarOnStart = false,
            goalValue = 0f,
            drawRoundCaps = true,
            useSingleColor = true,
            animate = OneShotValue(true),
        )

        return StatisticsDetailChartCompositeViewData(
            chartData = emptyChart.copy(
                visible = ranges.size > 1,
            ),
            compareChartData = emptyChart,
            showComparison = false,
            rangeAveragesTitle = " ",
            rangeAverages = if (ranges.size < 2) {
                emptyList()
            } else {
                mapToEmptyRangeAverages()
            },
            appliedChartGrouping = ChartGrouping.DAILY,
            chartGroupingViewData = emptyList(),
            chartGroupingVisible = availableChartGroupings.size > 1,
            appliedChartLength = ChartLength.TEN,
            chartLengthViewData = emptyList(),
            chartLengthVisible = availableChartLengths.isNotEmpty(),
            additionalChartButtonItems = emptyList(),
        )
    }

    fun mapToDailyChartViewData(
        data: Map<Int, Float>,
        firstDayOfWeek: DayOfWeek,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
        val days = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY,
        ).let { list ->
            list.indexOf(firstDayOfWeek)
                .takeUnless { it == -1 }.orZero()
                .let(list::rotateLeft)
        }

        val viewData = days.map { day ->
            val calendarDay = timeMapper.toCalendarDayOfWeek(day)
            BarChartView.ViewData(
                value = listOf(data[calendarDay].orZero() to Color.TRANSPARENT),
                legend = timeMapper.toShortDayOfWeekName(day),
            )
        }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    fun mapToHourlyChartViewData(
        data: Map<Int, Float>,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
        val hourLegends = (0 until 24).map {
            it to it.toString()
        }

        val viewData = hourLegends
            .map { (hour, legend) ->
                BarChartView.ViewData(
                    value = listOf(data[hour].orZero() to Color.TRANSPARENT),
                    legend = legend,
                )
            }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = false,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    fun mapToSplitChartGroupingViewData(
        rangeLength: RangeLength,
        splitChartGrouping: SplitChartGrouping,
    ): List<ViewHolderType> {
        val groupings = when (rangeLength) {
            is RangeLength.Day -> emptyList()
            else -> listOf(
                SplitChartGrouping.HOURLY,
                SplitChartGrouping.DAILY,
            )
        }

        return groupings.map {
            StatisticsDetailSplitGroupingViewData(
                splitChartGrouping = it,
                name = mapToSplitGroupingName(it),
                isSelected = it == splitChartGrouping,
            )
        }
    }

    fun mapToDurationsSlipChartViewData(
        data: Map<Range, Float>,
        isVisible: Boolean,
    ): StatisticsDetailChartViewData {
        val viewData = data
            .map { (range, percent) ->
                val started = timeMapper.formatDuration(range.timeStarted / 1000)
                val ended = timeMapper.formatDuration(range.timeEnded / 1000)
                range to BarChartView.ViewData(
                    value = listOf(percent to Color.TRANSPARENT),
                    legend = ended,
                    selectedBarLegend = "$started - $ended",
                )
            }.sortedBy { (range, _) ->
                range.timeStarted
            }.map { (_, data) ->
                data
            }

        return StatisticsDetailChartViewData(
            visible = isVisible,
            data = viewData,
            legendSuffix = SPLIT_CHART_LEGEND,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = true,
            showSelectedBarOnStart = false,
            goalValue = 0f,
            useSingleColor = true,
            drawRoundCaps = true,
            animate = OneShotValue(true),
        )
    }

    fun processPercentageString(value: Float): String {
        val text = when {
            value >= 10 -> value.toLong()
            (value * 10).roundToLong() % 10L == 0L -> value.toLong()
            else -> String.format("%.1f", value)
        }
        return "$text%"
    }

    private fun getRangeAverages(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        compareData: List<ChartBarDataDuration>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        chartGrouping: ChartGrouping,
        chartMode: ChartMode,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): Pair<String, List<StatisticsDetailCardInternalViewData>> {
        // No reason to show average of one value.
        if (data.size < 2 && compareData.size < 2) return "" to emptyList()

        fun getAverage(data: List<ChartBarDataDuration>): Long {
            if (data.isEmpty()) return 0L
            return data.sumOf { it.durations.map { it.first }.sum() } / data.size
        }

        fun formatInterval(
            interval: Long,
        ): String {
            return when (chartMode) {
                ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval,
                    forceSeconds = showSeconds,
                    useProportionalMinutes = useProportionalMinutes,
                )
                ChartMode.COUNTS -> interval.toString()
            }
        }

        val average = getAverage(data)
        val nonEmptyData = data.filter { it.durations.sumOf { it.first } != 0L }
        val averageByNonEmpty = getAverage(nonEmptyData)

        val comparisonAverage = getAverage(compareData)
        val comparisonNonEmptyData = compareData.filter { it.durations.sumOf { it.first } != 0L }
        val comparisonAverageByNonEmpty = getAverage(comparisonNonEmptyData)

        val prevAverage = getAverage(prevData)
        val prevNonEmptyData = prevData.filter { it.durations.sumOf { it.first } != 0L }
        val prevAverageByNonEmpty = getAverage(prevNonEmptyData)

        val title = resourceRepo.getString(
            R.string.statistics_detail_range_averages_title,
            mapToGroupingName(chartGrouping),
        )

        val rangeAverages = listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(average),
                valueChange = mapValueChange(
                    average = average,
                    prevAverage = prevAverage,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = formatInterval(comparisonAverage)
                    .let { "($it)" }
                    .takeIf { showComparison }
                    .orEmpty(),
                description = resourceRepo.getString(R.string.statistics_detail_range_averages),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(averageByNonEmpty),
                valueChange = mapValueChange(
                    average = averageByNonEmpty,
                    prevAverage = prevAverageByNonEmpty,
                    rangeLength = rangeLength,
                    isDarkTheme = isDarkTheme,
                ),
                secondValue = formatInterval(comparisonAverageByNonEmpty)
                    .let { "($it)" }
                    .takeIf { showComparison }
                    .orEmpty(),
                description = resourceRepo.getString(R.string.statistics_detail_range_averages_non_empty),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )

        return title to rangeAverages
    }

    private fun mapToEmptyRangeAverages(): List<StatisticsDetailCardInternalViewData> {
        val emptyValue by lazy { resourceRepo.getString(R.string.statistics_detail_empty) }

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_range_averages),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = emptyValue,
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_range_averages_non_empty),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }

    private fun mapValueChange(
        average: Long,
        prevAverage: Long,
        rangeLength: RangeLength,
        isDarkTheme: Boolean,
    ): StatisticsDetailCardInternalViewData.ValueChange {
        if (rangeLength == RangeLength.All) {
            return StatisticsDetailCardInternalViewData.ValueChange.None
        }

        val change: Float = when {
            prevAverage.orZero() == 0L && average.orZero() == 0L -> 0f
            prevAverage.orZero() == 0L && average.orZero() > 0L -> 100f
            prevAverage.orZero() == 0L && average.orZero() < 0L -> -100f
            prevAverage != 0L -> {
                (average.orZero() - prevAverage) * 100f / abs(prevAverage)
            }
            else -> 0f
        }

        fun formatChange(value: Float): String {
            val abs = abs(value)
            val text = when {
                abs >= 1_000_000f -> "∞"
                abs >= 1_000f -> "${(abs / 1000).toLong()}K"
                abs >= 10 -> abs.toLong()
                (abs * 10).roundToLong() % 10L == 0L -> abs.toLong()
                else -> String.format("%.1f", abs)
            }
            return if (value >= 0) "+$text%" else "-$text%"
        }

        return StatisticsDetailCardInternalViewData.ValueChange.Present(
            text = formatChange(change),
            color = if (change >= 0f) {
                colorMapper.toPositiveColor(isDarkTheme)
            } else {
                colorMapper.toNegativeColor(isDarkTheme)
            },
        )
    }

    private fun mapChartData(
        data: List<ChartBarDataDuration>,
        goal: Long,
        rangeLength: RangeLength,
        chartMode: ChartMode,
        showSelectedBarOnStart: Boolean,
        useSingleColor: Boolean,
        drawRoundCaps: Boolean,
    ): StatisticsDetailChartViewData {
        val (legendSuffix, isMinutes) = when (chartMode) {
            ChartMode.DURATIONS -> mapLegendSuffix(data)
            ChartMode.COUNTS -> "" to false
        }

        fun formatInterval(interval: Long): Float {
            return when (chartMode) {
                ChartMode.DURATIONS -> formatInterval(interval, isMinutes)
                ChartMode.COUNTS -> interval.toFloat()
            }
        }

        return StatisticsDetailChartViewData(
            visible = data.size > 1,
            data = data.map {
                val value = it.durations.map { (duration, color) ->
                    formatInterval(duration) to color
                }
                BarChartView.ViewData(
                    value = value,
                    legend = it.legend,
                )
            },
            legendSuffix = legendSuffix,
            addLegendToSelectedBar = true,
            shouldDrawHorizontalLegends = when (rangeLength) {
                is RangeLength.Day -> false
                is RangeLength.Week -> true
                is RangeLength.Month -> false
                is RangeLength.Year -> data.size <= 12
                is RangeLength.All,
                is RangeLength.Custom,
                is RangeLength.Last,
                -> data.size <= 10
            },
            showSelectedBarOnStart = showSelectedBarOnStart,
            goalValue = formatInterval(goal),
            useSingleColor = useSingleColor,
            drawRoundCaps = drawRoundCaps,
            animate = OneShotValue(true),
        )
    }

    fun mapGoalData(
        data: List<ChartBarDataDuration>,
        goalValue: Long,
        goalSubtype: RecordTypeGoal.Subtype,
        isDarkTheme: Boolean,
    ): List<ChartBarDataDuration> {
        if (goalValue == 0L) return emptyList()
        val greenColor = resourceRepo.getThemedAttr(R.attr.appPositiveColor, isDarkTheme)
        val redColor = resourceRepo.getThemedAttr(R.attr.appNegativeColor, isDarkTheme)
        val positiveColor = when (goalSubtype) {
            is RecordTypeGoal.Subtype.Goal -> greenColor
            is RecordTypeGoal.Subtype.Limit -> redColor
        }
        val negativeColor = when (goalSubtype) {
            is RecordTypeGoal.Subtype.Goal -> redColor
            is RecordTypeGoal.Subtype.Limit -> greenColor
        }

        return data.map { dataPart ->
            val totalDuration = dataPart.durations.sumOf { it.first }
            // Show difference from goal value only on days
            // when there were records tracked.
            val goalDuration = if (totalDuration != 0L) totalDuration - goalValue else 0L
            val color = if (goalDuration >= 0) positiveColor else negativeColor
            ChartBarDataDuration(
                legend = dataPart.legend,
                durations = listOf(goalDuration to color),
            )
        }
    }

    fun mapGoalStatsViewData(
        records: List<RecordBase>,
        currentRangeGoal: RecordTypeGoal?,
        rangeLength: RangeLength,
        rangePosition: Int,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<ViewHolderType> {
        val goalValue = getGoalValue(currentRangeGoal)
        val goalSubtype = currentRangeGoal?.subtype ?: RecordTypeGoal.Subtype.Goal
        val goalRange = currentRangeGoal?.range ?: RecordTypeGoal.Range.Daily
        if (goalValue == 0L) return emptyList()

        val items = mutableListOf<ViewHolderType>()
        val chartMode = mapToChartMode(currentRangeGoal)
        val goalStats = mapGoalStats(
            records = records,
            goalValue = goalValue,
            goalRange = goalRange,
            rangeLength = rangeLength,
            rangePosition = rangePosition,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        if (goalStats.isNotEmpty()) {
            val title = when (goalSubtype) {
                is RecordTypeGoal.Subtype.Goal -> R.string.change_record_type_goal_time_hint
                is RecordTypeGoal.Subtype.Limit -> R.string.change_record_type_limit_time_hint
            }.let(resourceRepo::getString)
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalStats,
                title = title,
                marginTopDp = 10,
                data = goalStats,
            )
        }

        return items
    }

    fun mapGoalChartViewData(
        data: List<ChartBarDataDuration>,
        prevData: List<ChartBarDataDuration>,
        chartGoal: RecordTypeGoal?,
        rangeLength: RangeLength,
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
        chartMode: ChartMode,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val goalValue = getGoalValue(chartGoal)
        if (goalValue == 0L) return emptyList()

        val items = mutableListOf<ViewHolderType>()
        val goalSubtype = chartGoal?.subtype ?: RecordTypeGoal.Subtype.Goal
        val goalData = mapGoalData(
            data = data,
            goalValue = goalValue,
            goalSubtype = goalSubtype,
            isDarkTheme = isDarkTheme,
        )
        val goalChartPrevData = mapGoalData(
            data = prevData,
            goalValue = goalValue,
            goalSubtype = goalSubtype,
            isDarkTheme = isDarkTheme,
        )
        val chartData = mapChartData(
            data = goalData,
            goal = 0, // Don't show goal on goal graph.
            rangeLength = rangeLength,
            chartMode = chartMode,
            showSelectedBarOnStart = true,
            useSingleColor = false,
            drawRoundCaps = true,
        )
        val (title, rangeAverages) = getRangeAverages(
            data = goalData,
            prevData = goalChartPrevData,
            compareData = emptyList(),
            showComparison = false,
            rangeLength = rangeLength,
            chartGrouping = appliedChartGrouping,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
            isDarkTheme = isDarkTheme,
        )
        val chartGroupingViewData = mapToChartGroupingViewData(
            availableChartGroupings = availableChartGroupings,
            appliedChartGrouping = appliedChartGrouping,
        )
        val chartLengthViewData = mapToChartLengthViewData(
            availableChartLengths = availableChartLengths,
            appliedChartLength = appliedChartLength,
        )
        val goalTotals = mapGoalExcessDeficitTotals(
            goalData = goalData,
            chartMode = chartMode,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )

        if (chartData.visible) {
            items += StatisticsDetailHintViewData(
                block = StatisticsDetailBlock.GoalExcessDeficitHint,
                text = resourceRepo.getString(R.string.statistics_detail_goals_hint),
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailBarChartViewData(
                block = StatisticsDetailBlock.GoalChartData,
                singleColor = null,
                marginTopDp = 0,
                data = chartData,
            )
        }

        if (chartGroupingViewData.size > 1) {
            items += StatisticsDetailButtonsRowViewData(
                block = StatisticsDetailBlock.GoalChartGrouping,
                marginTopDp = 4,
                data = chartGroupingViewData,
            )
        }

        if (chartLengthViewData.isNotEmpty()) {
            // Update margin top depending if has buttons before.
            val hasButtonsBefore = items.lastOrNull() is StatisticsDetailButtonsRowViewData
            val marginTopDp = if (hasButtonsBefore) -10 else 4
            items += StatisticsDetailButtonsRowViewData(
                block = StatisticsDetailBlock.GoalChartLength,
                marginTopDp = marginTopDp,
                data = chartLengthViewData,
            )
        }

        if (rangeAverages.isNotEmpty()) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalRangeAverages,
                title = title,
                marginTopDp = 0,
                data = rangeAverages,
            )
        }

        if (chartData.visible) {
            items += StatisticsDetailCardViewData(
                block = StatisticsDetailBlock.GoalTotals,
                title = "",
                marginTopDp = 0,
                data = goalTotals,
            )
        }

        return items
    }

    fun mapLegendSuffix(
        data: List<ChartBarDataDuration>,
    ): Pair<String, Boolean> {
        val isMinutes = data
            .maxOfOrNull { barPart -> abs(barPart.durations.sumOf { it.first }) }
            .orZero()
            .let(TimeUnit.MILLISECONDS::toHours) == 0L

        val legendSuffix = if (isMinutes) {
            R.string.statistics_detail_legend_minute_suffix
        } else {
            R.string.statistics_detail_legend_hour_suffix
        }.let(resourceRepo::getString)

        return legendSuffix to isMinutes
    }

    fun formatInterval(interval: Long, isMinutes: Boolean): Float {
        val hr: Long = TimeUnit.MILLISECONDS.toHours(
            interval,
        )
        val min: Long = TimeUnit.MILLISECONDS.toMinutes(
            interval - TimeUnit.HOURS.toMillis(hr),
        )
        val sec: Long = TimeUnit.MILLISECONDS.toSeconds(
            interval - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min),
        )

        return if (isMinutes) {
            hr * 60f + min + sec / 60f
        } else {
            hr + min / 60f
        }
    }

    fun mapToChartMode(
        goal: RecordTypeGoal?,
    ): ChartMode {
        return when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> ChartMode.DURATIONS
            is RecordTypeGoal.Type.Count -> ChartMode.COUNTS
            null -> ChartMode.DURATIONS
        }
    }

    private fun getGoalValue(
        goal: RecordTypeGoal?,
    ): Long {
        return when (goal?.type) {
            is RecordTypeGoal.Type.Duration -> goal.value * 1000
            is RecordTypeGoal.Type.Count -> goal.value
            null -> 0L
        }
    }

    private fun mapSplitByActivityItems(
        splitByActivity: Boolean,
        splitSortMode: ChartSplitSortMode,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        return StatisticsDetailButtonViewData(
            marginTopDp = 0, // Set later depending on previous items in list.
            data = StatisticsDetailButtonViewData.Button(
                block = StatisticsDetailBlock.ChartSplitByActivity,
                text = resourceRepo.getString(R.string.statistics_detail_chart_split),
                color = if (splitByActivity) {
                    resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme)
                } else {
                    resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme)
                },
            ),
            dataSecond = if (splitByActivity) {
                StatisticsDetailButtonViewData.Button(
                    block = StatisticsDetailBlock.ChartSplitByActivitySort,
                    text = when (splitSortMode) {
                        ChartSplitSortMode.ACTIVITY_ORDER ->
                            resourceRepo.getString(R.string.settings_sort_activity)
                        ChartSplitSortMode.DURATION ->
                            resourceRepo.getString(R.string.records_all_sort_duration)
                    },
                    color = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
                )
            } else {
                null
            },
        ).let(::listOf)
    }

    private fun mapGoalExcessDeficitTotals(
        goalData: List<ChartBarDataDuration>,
        chartMode: ChartMode,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): List<StatisticsDetailCardInternalViewData> {
        val barValues = goalData.map { bar -> bar.durations.sumOf { it.first } }
        val negativeValue = barValues.filter { it < 0L }.sum()
        val positiveValue = barValues.filter { it > 0L }.sum()
        val total = negativeValue + positiveValue

        fun formatInterval(
            interval: Long,
        ): String {
            return when (chartMode) {
                ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval,
                    forceSeconds = showSeconds,
                    useProportionalMinutes = useProportionalMinutes,
                )
                ChartMode.COUNTS -> interval.toString()
            }
        }

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(negativeValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_goals_deficit),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(total),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(positiveValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = resourceRepo.getString(R.string.statistics_detail_goals_excess),
                titleTextSizeSp = 14,
                subtitleTextSizeSp = 12,
            ),
        )
    }

    private fun mapGoalStats(
        records: List<RecordBase>,
        goalValue: Long,
        goalRange: RecordTypeGoal.Range,
        rangeLength: RangeLength,
        rangePosition: Int,
        chartMode: ChartMode,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        firstDayOfWeek: DayOfWeek,
        startOfDayShift: Long,
    ): List<StatisticsDetailCardInternalViewData> {
        fun formatInterval(
            interval: Long,
        ): String {
            return when (chartMode) {
                ChartMode.DURATIONS -> timeMapper.formatInterval(
                    interval = interval,
                    forceSeconds = showSeconds,
                    useProportionalMinutes = useProportionalMinutes,
                )
                ChartMode.COUNTS -> interval.toString()
            }
        }

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        val recordsFromRange = if (range.timeStarted == 0L && range.timeEnded == 0L) {
            records
        } else {
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampRecordToRange(it, range) }
        }
        val currentValue = when (chartMode) {
            ChartMode.DURATIONS -> recordsFromRange.sumOf(RecordBase::duration)
            ChartMode.COUNTS -> recordsFromRange.size.toLong()
        }
        val percentage = if (goalValue != 0L) {
            currentValue * 100f / goalValue
        } else {
            0f
        }
        val percentageString = processPercentageString(percentage)
        val description = when (goalRange) {
            is RecordTypeGoal.Range.Session -> 0 // Shouldn't be possible.
            is RecordTypeGoal.Range.Daily -> R.string.range_day
            is RecordTypeGoal.Range.Weekly -> R.string.range_week
            is RecordTypeGoal.Range.Monthly -> R.string.range_month
        }.let(resourceRepo::getString)

        return listOf(
            StatisticsDetailCardInternalViewData(
                value = formatInterval(goalValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = description,
            ),
            StatisticsDetailCardInternalViewData(
                value = formatInterval(currentValue),
                valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                secondValue = "",
                description = percentageString,
            ),
        )
    }

    private fun mapToChartGroupingViewData(
        availableChartGroupings: List<ChartGrouping>,
        appliedChartGrouping: ChartGrouping,
    ): List<ViewHolderType> {
        return availableChartGroupings.map {
            StatisticsDetailGroupingViewData(
                chartGrouping = it,
                name = mapToGroupingName(it),
                isSelected = it == appliedChartGrouping,
                textSizeSp = if (availableChartGroupings.size >= 3) 12 else null,
            )
        }
    }

    private fun mapToChartLengthViewData(
        availableChartLengths: List<ChartLength>,
        appliedChartLength: ChartLength,
    ): List<ViewHolderType> {
        return availableChartLengths.map {
            StatisticsDetailChartLengthViewData(
                chartLength = it,
                name = mapToLengthName(it),
                isSelected = it == appliedChartLength,
            )
        }
    }

    private fun mapToGroupingName(chartGrouping: ChartGrouping): String {
        return when (chartGrouping) {
            ChartGrouping.DAILY -> R.string.statistics_detail_chart_daily
            ChartGrouping.WEEKLY -> R.string.statistics_detail_chart_weekly
            ChartGrouping.MONTHLY -> R.string.statistics_detail_chart_monthly
            ChartGrouping.YEARLY -> R.string.statistics_detail_chart_yearly
        }.let(resourceRepo::getString)
    }

    private fun mapToSplitGroupingName(splitChartGrouping: SplitChartGrouping): String {
        return when (splitChartGrouping) {
            SplitChartGrouping.HOURLY -> R.string.statistics_detail_chart_hourly
            SplitChartGrouping.DAILY -> R.string.statistics_detail_chart_daily
        }.let(resourceRepo::getString)
    }

    private fun mapToLengthName(chartLength: ChartLength): String {
        return when (chartLength) {
            ChartLength.TEN -> R.string.statistics_detail_length_ten
            ChartLength.FIFTY -> R.string.statistics_detail_length_fifty
            ChartLength.HUNDRED -> R.string.statistics_detail_length_hundred
        }.let(resourceRepo::getString)
    }

    companion object {
        private const val SPLIT_CHART_LEGEND = "%"
    }
}