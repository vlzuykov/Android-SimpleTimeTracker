package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import javax.inject.Inject

class CalculateAdjacentActivitiesInteractor @Inject constructor() {

    // Doesn't count multitasked activities.
    // Only those that started after current ended.
    fun calculateNextActivities(
        typeIds: List<Long>,
        records: List<Record>,
    ): Map<Long, List<CalculationResult>> {
        val counts = mutableMapOf<Long, MutableMap<Long, Long>>()
        val recordsSorted = records.sortedBy { it.timeStarted }
        val currentRecords: MutableMap<Long, Record> = mutableMapOf()

        recordsSorted.forEach { record ->
            val typeId = record.typeId
            val idsToRemoveFromCurrents = mutableListOf<Long>()

            currentRecords.values.forEach { currentRecord ->
                val currentTimeEnded = currentRecord.timeEnded
                if (currentTimeEnded <= record.timeStarted) {
                    counts[currentRecord.typeId] = counts[currentRecord.typeId]
                        .orEmpty().toMutableMap()
                        .also { it[typeId] = it[typeId].orZero() + 1 }
                    idsToRemoveFromCurrents += currentRecord.typeId
                }
            }

            idsToRemoveFromCurrents.forEach(currentRecords::remove)
            if (typeId !in currentRecords && typeId in typeIds) {
                currentRecords[typeId] = record
            }
        }

        return counts.mapValues { (_, counts) ->
            counts.keys
                .sortedByDescending { counts[it].orZero() }
                .take(MAX_COUNT)
                .map { CalculationResult(it, counts[it].orZero()) }
        }
    }

    // TODO make more precise calculations?
    fun calculateMultitasking(
        typeId: Long,
        records: List<RecordBase>,
    ): List<CalculationResult> {
        val counts = mutableMapOf<Long, Long>()

        val recordsSorted = records.sortedBy { it.timeStarted }
        var currentRecord: RecordBase? = null
        recordsSorted.forEach { record ->
            val currentTimeStarted = currentRecord?.timeStarted
            val currentTimeEnded = currentRecord?.timeEnded
            if (currentTimeStarted != null &&
                currentTimeEnded != null &&
                // Find next records that was started after this one but before this one ends.
                currentTimeStarted <= record.timeStarted &&
                currentTimeEnded > record.timeStarted &&
                // Cutoff short intersections.
                currentTimeEnded - record.timeStarted > 1_000L
            ) {
                record.typeIds.firstOrNull()?.let { id ->
                    counts[id] = counts[id].orZero() + 1
                }
            }
            if (typeId in record.typeIds) {
                currentRecord = record
            }
        }

        return counts.keys
            .sortedByDescending { counts[it].orZero() }
            .take(MAX_COUNT)
            .map { CalculationResult(it, counts[it].orZero()) }
    }

    data class CalculationResult(
        val typeId: Long,
        val count: Long,
    )

    companion object {
        private const val MAX_COUNT = 5
    }
}