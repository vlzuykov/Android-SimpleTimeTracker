package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class GetRangeInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
) {

    suspend fun getRange(rangeLength: RangeLength): Range {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()

        return timeMapper.getRangeStartAndEnd(
            rangeLength = rangeLength,
            shift = 0,
            firstDayOfWeek = firstDayOfWeek,
            startOfDayShift = startOfDayShift,
        )
    }
}