package com.example.util.simpletimetracker.domain.statistics.interactor

import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.statistics.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val rangeMapper: RangeMapper,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = getRecords(range)
        val activityToRecords = getActivityRecords(records)

        getStatistics(range, activityToRecords).plus(
            getUntracked(range, records, addUntracked),
        )
    }

    suspend fun getRecords(range: Range): List<RecordBase> {
        val runningRecords = runningRecordInteractor.getAll()

        return if (rangeIsAllRecords(range)) {
            recordInteractor.getAll() + runningRecords
        } else {
            recordInteractor.getFromRange(range) +
                rangeMapper.getRunningRecordsFromRange(runningRecords, range)
        }
    }

    private fun getActivityRecords(
        allRecords: List<RecordBase>,
    ): Map<Long, List<RecordBase>> {
        return allRecords.groupBy {
            it.typeIds.firstOrNull().orZero() // Multitask is not available in statistics.
        }
    }

    fun getActivityRecordsFull(
        allRecords: List<RecordBase>,
    ): Map<Long, List<RecordBase>> {
        val activities: MutableMap<Long, MutableList<RecordBase>> = mutableMapOf()

        allRecords.forEach { record ->
            record.typeIds.forEach { typeId ->
                activities.getOrPut(typeId) { mutableListOf() }.add(record)
            }
        }

        return activities
    }

    fun getStatistics(
        range: Range,
        records: Map<Long, List<RecordBase>>,
    ): List<Statistics> {
        return records.map { (id, records) ->
            Statistics(
                id = id,
                data = getStatisticsData(range, records),
            )
        }
    }

    fun getStatisticsData(
        allRecords: Map<Long, List<RecordBase>>,
    ): List<Statistics> {
        return allRecords.map { (id, records) ->
            Statistics(
                id = id,
                data = mapStatisticsItem(records),
            )
        }
    }

    private fun getStatisticsData(
        range: Range,
        records: List<RecordBase>,
    ): Statistics.Data {
        // If range is all records - do not clamp to range.
        return if (rangeIsAllRecords(range)) {
            mapStatisticsItem(records)
        } else {
            // Remove parts of the record that is not in the range
            rangeMapper.getRecordsFromRange(records, range)
                .map { rangeMapper.clampToRange(it, range) }
                .let {
                    Statistics.Data(
                        duration = it.sumOf(Range::duration),
                        count = it.size.toLong(),
                    )
                }
        }
    }

    suspend fun getUntracked(
        range: Range,
        records: List<RecordBase>,
        addUntracked: Boolean,
    ): List<Statistics> {
        if (addUntracked) {
            val untrackedRanges = getUntrackedRecordsInteractor.get(
                range = range,
                records = records.map(RecordBase::toRange),
            )
            val untrackedTime = untrackedRanges.sumOf { it.duration }
            val untrackedCount = untrackedRanges.size

            if (untrackedTime > 0L) {
                return Statistics(
                    id = UNTRACKED_ITEM_ID,
                    data = Statistics.Data(
                        duration = untrackedTime,
                        count = untrackedCount.toLong(),
                    ),
                ).let(::listOf)
            }
        }

        return emptyList()
    }

    private fun rangeIsAllRecords(range: Range): Boolean {
        return range.timeStarted == 0L && range.timeEnded == 0L
    }

    private fun mapStatisticsItem(
        records: List<RecordBase>,
    ): Statistics.Data {
        return Statistics.Data(
            duration = records.sumOf(RecordBase::duration),
            count = records.size.toLong(),
        )
    }
}