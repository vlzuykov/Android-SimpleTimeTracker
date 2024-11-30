package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RecordBase
import com.example.util.simpletimetracker.domain.model.Statistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatisticsTagInteractor @Inject constructor(
    private val statisticsInteractor: StatisticsInteractor,
) {

    suspend fun getFromRange(
        range: Range,
        addUntracked: Boolean,
        addUncategorized: Boolean,
    ): List<Statistics> = withContext(Dispatchers.IO) {
        val records = statisticsInteractor.getRecords(range)

        getTagRecords(records, addUncategorized)
            .let {
                statisticsInteractor.getStatistics(range, it)
            }
            .plus(
                statisticsInteractor.getUntracked(range, records, addUntracked),
            )
    }

    fun getTagRecords(
        allRecords: List<RecordBase>,
        addUncategorized: Boolean,
    ): Map<Long, List<RecordBase>> {
        val tags: MutableMap<Long, MutableList<RecordBase>> = mutableMapOf()

        allRecords.forEach { record ->
            record.tagIds.forEach { tagId ->
                tags.getOrPut(tagId) { mutableListOf() }.add(record)
            }
            if (addUncategorized && record.tagIds.isEmpty()) {
                tags.getOrPut(UNCATEGORIZED_ITEM_ID) { mutableListOf() }.add(record)
            }
        }

        return tags
    }
}