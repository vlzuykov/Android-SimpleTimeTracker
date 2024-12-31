package com.example.util.simpletimetracker.domain.activitySuggestion.interactor

import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class GetCurrentActivitySuggestionsInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val activitySuggestionInteractor: ActivitySuggestionInteractor,
) {

    suspend fun execute(
        recordTypesMap: Map<Long, RecordType>,
        runningRecords: List<RunningRecord>,
    ): List<RecordType> {
        return execute(runningRecords).mapNotNull { typeId ->
            recordTypesMap[typeId]?.takeIf { !it.hidden }
        }
    }

    private suspend fun execute(
        runningRecords: List<RunningRecord>,
    ): List<Long> {
        val currentOrLast = runningRecords.minByOrNull { it.timeStarted }
            ?: recordInteractor.getAllPrev(System.currentTimeMillis()).firstOrNull()

        val currentOrLastTypeId = currentOrLast?.typeIds?.firstOrNull()

        return currentOrLastTypeId
            ?.let { activitySuggestionInteractor.getByTypeId(it) }
            ?.firstOrNull()
            ?.suggestionIds
            .orEmpty()
    }
}