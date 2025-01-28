package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetCurrentDayInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import java.util.Calendar
import javax.inject.Inject

class GetCurrentDayInteractorImpl @Inject constructor(
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) : GetCurrentDayInteractor {

    override suspend fun execute(timestamp: Long): DayOfWeek {
        return timeMapper.getDayOfWeek(
            timestamp = timestamp,
            calendar = Calendar.getInstance(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
        )
    }
}