package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionGraph
import com.example.util.simpletimetracker.feature_statistics_detail.model.DataDistributionMode
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableLongest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableShortest
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailClickableTracked
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailStatsViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailStatsInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val resourceRepo: ResourceRepo,
    private val dataDistributionInteractor: StatisticsDetailDataDistributionInteractor,
) {

    suspend fun getStatsViewData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        showComparison: Boolean,
        rangeLength: RangeLength,
        rangePosition: Int,
        dataDistributionMode: DataDistributionMode,
        dataDistributionGraph: DataDistributionGraph,
    ): StatisticsDetailStatsViewData = withContext(Dispatchers.Default) {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val types = recordTypeInteractor.getAll()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )

        return@withContext mapStatsData(
            records = if (range.timeStarted == 0L && range.timeEnded == 0L) {
                records
            } else {
                rangeMapper.getRecordsFromRange(records, range)
                    .map { rangeMapper.clampRecordToRange(it, range) }
            },
            compareRecords = if (range.timeStarted == 0L && range.timeEnded == 0L) {
                compareRecords
            } else {
                rangeMapper.getRecordsFromRange(compareRecords, range)
                    .map { rangeMapper.clampRecordToRange(it, range) }
            },
            showComparison = showComparison,
            types = types,
            dataDistributionMode = dataDistributionMode,
            dataDistributionGraph = dataDistributionGraph,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )
    }

    fun getEmptyStatsViewData(): StatisticsDetailStatsViewData {
        return mapToStatsViewData(
            totalDuration = "",
            compareTotalDuration = "",
            timesTracked = null,
            compareTimesTracked = "",
            timesTrackedIcon = null,
            shortestRecord = "",
            compareShortestRecord = "",
            shortestRecordDate = "",
            averageRecord = "",
            compareAverageRecord = "",
            longestRecord = "",
            compareLongestRecord = "",
            longestRecordDate = "",
            firstRecord = "",
            compareFirstRecord = "",
            lastRecord = "",
            compareLastRecord = "",
            splitData = emptyList(),
        )
    }

    private suspend fun mapStatsData(
        records: List<RecordBase>,
        compareRecords: List<RecordBase>,
        showComparison: Boolean,
        types: List<RecordType>,
        dataDistributionMode: DataDistributionMode,
        dataDistributionGraph: DataDistributionGraph,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
    ): StatisticsDetailStatsViewData {
        val typesMap = types.associateBy { it.id }
        val recordsSorted = records.sortedBy { it.timeStarted }
        val durations = records.map(RecordBase::duration)

        val compareRecordsSorted = compareRecords.sortedBy { it.timeStarted }
        val compareDurations = compareRecords.map(RecordBase::duration)

        val shortestRecord = records.minByOrNull(RecordBase::duration)
        val longestRecord = records.maxByOrNull(RecordBase::duration)

        val emptyValue by lazy {
            resourceRepo.getString(R.string.statistics_detail_empty)
        }
        val recordsAllIcon = StatisticsDetailCardInternalViewData.Icon(
            iconDrawable = R.drawable.statistics_detail_records_all,
            iconColor = if (isDarkTheme) {
                R.color.colorInactiveDark
            } else {
                R.color.colorInactive
            }.let(resourceRepo::getColor),
        )
        val splitData = dataDistributionInteractor.mapDataDistribution(
            mode = dataDistributionMode,
            graph = dataDistributionGraph,
            records = records,
            typesMap = typesMap,
            isDarkTheme = isDarkTheme,
            useProportionalMinutes = useProportionalMinutes,
            showSeconds = showSeconds,
        )

        fun formatInterval(value: Long?): String {
            value ?: return emptyValue
            return timeMapper.formatInterval(
                interval = value,
                forceSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            )
        }

        fun formatDateTimeYear(value: Long?): String {
            value ?: return emptyValue
            return timeMapper.formatDateTimeYear(value, useMilitaryTime)
        }

        fun getAverage(values: List<Long>): Long? {
            return if (values.isNotEmpty()) {
                values.sum() / values.size
            } else {
                null
            }
        }

        fun processComparisonString(value: String): String {
            return value
                .takeIf { showComparison }
                ?.let { "($it)" }
                .orEmpty()
        }

        fun processLengthHint(value: RecordBase?): String {
            value ?: return emptyValue

            val result = StringBuilder()
            value.typeIds
                .mapNotNull(typesMap::get)
                .map(RecordType::name)
                .takeUnless { it.isEmpty() }
                ?.joinToString()
                ?.let {
                    result.append(it)
                    result.append("\n")
                }
            value.timeStarted
                .let(::formatDateTimeYear)
                .let { result.append(it) }

            return result.toString()
        }

        return mapToStatsViewData(
            totalDuration = durations.sum()
                .let(::formatInterval),
            compareTotalDuration = compareDurations.sum()
                .let(::formatInterval)
                .let(::processComparisonString),
            timesTracked = records.size,
            compareTimesTracked = compareRecords.size.toString()
                .let(::processComparisonString),
            timesTrackedIcon = recordsAllIcon,
            shortestRecord = shortestRecord?.duration
                .let(::formatInterval),
            compareShortestRecord = compareDurations.minOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            shortestRecordDate = shortestRecord
                .let(::processLengthHint),
            averageRecord = getAverage(durations)
                .let(::formatInterval),
            compareAverageRecord = getAverage(compareDurations)
                .let(::formatInterval)
                .let(::processComparisonString),
            longestRecord = longestRecord?.duration
                .let(::formatInterval),
            compareLongestRecord = compareDurations.maxOrNull()
                .let(::formatInterval)
                .let(::processComparisonString),
            longestRecordDate = longestRecord
                .let(::processLengthHint),
            firstRecord = recordsSorted.firstOrNull()?.timeStarted
                .let(::formatDateTimeYear),
            compareFirstRecord = compareRecordsSorted.firstOrNull()?.timeStarted
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            lastRecord = recordsSorted.lastOrNull()?.timeEnded
                .let(::formatDateTimeYear),
            compareLastRecord = compareRecordsSorted.lastOrNull()?.timeEnded
                .let(::formatDateTimeYear)
                .let(::processComparisonString),
            splitData = splitData,
        )
    }

    private fun mapToStatsViewData(
        totalDuration: String,
        compareTotalDuration: String,
        timesTracked: Int?,
        compareTimesTracked: String,
        timesTrackedIcon: StatisticsDetailCardInternalViewData.Icon?,
        shortestRecord: String,
        compareShortestRecord: String,
        shortestRecordDate: String,
        averageRecord: String,
        compareAverageRecord: String,
        longestRecord: String,
        compareLongestRecord: String,
        longestRecordDate: String,
        firstRecord: String,
        compareFirstRecord: String,
        lastRecord: String,
        compareLastRecord: String,
        splitData: List<ViewHolderType>,
    ): StatisticsDetailStatsViewData {
        return StatisticsDetailStatsViewData(
            totalDuration = listOf(
                StatisticsDetailCardInternalViewData(
                    value = totalDuration,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareTotalDuration,
                    description = resourceRepo.getString(R.string.statistics_detail_total_duration),
                    accented = true,
                    titleTextSizeSp = 22,
                ),
            ),
            timesTracked = listOf(
                StatisticsDetailCardInternalViewData(
                    value = timesTracked?.toString() ?: "",
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareTimesTracked,
                    description = resourceRepo.getQuantityString(
                        R.plurals.statistics_detail_times_tracked, timesTracked.orZero(),
                    ),
                    icon = timesTrackedIcon,
                    clickable = StatisticsDetailClickableTracked,
                    accented = true,
                    titleTextSizeSp = 22,
                ),
            ),
            averageRecord = listOf(
                StatisticsDetailCardInternalViewData(
                    value = shortestRecord,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareShortestRecord,
                    description = resourceRepo.getString(R.string.statistics_detail_shortest_record),
                    clickable = StatisticsDetailClickableShortest(shortestRecordDate),
                ),
                StatisticsDetailCardInternalViewData(
                    value = averageRecord,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareAverageRecord,
                    description = resourceRepo.getString(R.string.statistics_detail_average_record),
                ),
                StatisticsDetailCardInternalViewData(
                    value = longestRecord,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareLongestRecord,
                    description = resourceRepo.getString(R.string.statistics_detail_longest_record),
                    clickable = StatisticsDetailClickableLongest(longestRecordDate),
                ),
            ),
            datesTracked = listOf(
                StatisticsDetailCardInternalViewData(
                    value = firstRecord,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareFirstRecord,
                    description = resourceRepo.getString(R.string.statistics_detail_first_record),
                ),
                StatisticsDetailCardInternalViewData(
                    value = lastRecord,
                    valueChange = StatisticsDetailCardInternalViewData.ValueChange.None,
                    secondValue = compareLastRecord,
                    description = resourceRepo.getString(R.string.statistics_detail_last_record),
                ),
            ),
            splitData = splitData,
        )
    }
}