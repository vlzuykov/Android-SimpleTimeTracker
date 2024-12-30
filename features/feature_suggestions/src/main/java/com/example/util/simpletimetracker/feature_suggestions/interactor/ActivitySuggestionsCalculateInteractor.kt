package com.example.util.simpletimetracker.feature_suggestions.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.CalculateAdjacentActivitiesInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_suggestions.model.ActivitySuggestionModel
import javax.inject.Inject

class ActivitySuggestionsCalculateInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordInteractor: RecordInteractor,
    private val calculateAdjacentActivitiesInteractor: CalculateAdjacentActivitiesInteractor,
) {

    suspend fun execute(
        typeIds: List<Long>,
    ): List<ActivitySuggestionModel> {
        // TODO SUG selectable range?
        val range = timeMapper.getRangeStartAndEnd(
            rangeLength = RangeLength.Year,
            shift = 0,
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
        )
        val records = recordInteractor.getFromRange(range)

        val data = calculateAdjacentActivitiesInteractor.calculateNextActivities(
            typeIds = typeIds,
            records = records,
            maxCount = Int.MAX_VALUE,
        )

        return typeIds.mapNotNull { typeId ->
            val thisTypeSuggestions = data[typeId]
                ?: return@mapNotNull null
            ActivitySuggestionModel(
                typeId = typeId,
                suggestions = thisTypeSuggestions.map { it.typeId },
            )
        }
    }
}