package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import javax.inject.Inject

class GetTimeToDayEndInteractor @Inject constructor(
    private val timeMapper: TimeMapper,
) {

    fun execute(): Long {
        val current = System.currentTimeMillis()

        return timeMapper
            .getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = 0,
                firstDayOfWeek = DayOfWeek.MONDAY, // not needed.
                startOfDayShift = 0, // not needed.
            )
            .timeEnded
            .let { it - current }
            .takeIf { it > 0 }
            ?: current
    }
}