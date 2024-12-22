package com.example.util.simpletimetracker.feature_statistics_detail.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getTypeIds
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CalculateAdjacentActivitiesInteractor
import com.example.util.simpletimetracker.domain.interactor.CalculateAdjacentActivitiesInteractor.CalculationResult
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailViewDataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsDetailAdjacentActivitiesInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper,
    private val statisticsDetailViewDataMapper: StatisticsDetailViewDataMapper,
    private val calculateAdjacentActivitiesInteractor: CalculateAdjacentActivitiesInteractor,
) {

    suspend fun getNextActivitiesViewData(
        filter: List<RecordsFilter>,
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<ViewHolderType> = withContext(Dispatchers.Default) {
        // Show only if one activity is selected.
        val typeId = getActivityId(filter) ?: return@withContext getEmptyViewData()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val actualRecords = getRecords(rangeLength, rangePosition)
        val nextActivitiesIds = calculateAdjacentActivitiesInteractor
            .calculateNextActivities(typeId, actualRecords)
        val multitaskingActivitiesIds = calculateAdjacentActivitiesInteractor
            .calculateMultitasking(typeId, actualRecords)

        fun mapPreviews(typeToCounts: List<CalculationResult>): List<ViewHolderType> {
            val total = typeToCounts.sumOf(CalculationResult::count)
                .takeUnless { it == 0L }
                ?: return emptyList()

            val typeToPercents = typeToCounts.map { result ->
                result.typeId to (result.count * 100 / total)
            }

            return typeToPercents.mapIndexedNotNull { index, (typeId, percent) ->
                // So that all items would sum up to 100,
                // adjust value of the first element according to all the rest elements.
                val isFirst = index == 0
                val correctedPercent = if (isFirst) {
                    100 - typeToPercents.drop(1).sumOf { it.second }
                } else {
                    percent
                }

                statisticsDetailViewDataMapper.mapToPreview(
                    recordType = recordTypes[typeId] ?: return@mapIndexedNotNull null,
                    isDarkTheme = isDarkTheme,
                    isFirst = false,
                    isForComparison = false,
                ).copy(name = "$correctedPercent%")
            }
        }

        val nextActivities = nextActivitiesIds
            .let(::mapPreviews)
        val nextActivitiesHint = resourceRepo
            .getString(R.string.statistics_detail_next_activities_hint)
            .let(::HintViewData)
            .takeIf { nextActivities.isNotEmpty() }
            .let(::listOfNotNull)

        val multitaskingActivities = multitaskingActivitiesIds
            .let(::mapPreviews)
        val multitaskingActivitiesHint = resourceRepo
            .getString(R.string.statistics_detail_multitasking_activities_hint)
            .let(::HintViewData)
            .takeIf { multitaskingActivities.isNotEmpty() }
            .let(::listOfNotNull)

        return@withContext nextActivitiesHint +
            nextActivities +
            multitaskingActivitiesHint +
            multitaskingActivities
    }

    // Don't use records from viewModel because they are already filtered,
    // we need all records here.
    private suspend fun getRecords(
        rangeLength: RangeLength,
        rangePosition: Int,
    ): List<RecordBase> {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = rangePosition,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
        return if (range.timeStarted == 0L && range.timeEnded == 0L) {
            recordInteractor.getAll()
        } else {
            recordInteractor.getFromRange(range)
        }
    }

    private fun getActivityId(
        filter: List<RecordsFilter>,
    ): Long? {
        return filter.getTypeIds()
            .takeIf { it.size == 1 }
            ?.firstOrNull()
    }

    private fun getEmptyViewData(): List<ViewHolderType> {
        return emptyList()
    }
}