package com.example.util.simpletimetracker.domain.activityFilter.interactor

import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import javax.inject.Inject

class ChangeSelectedActivityFilterMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
) {

    suspend fun onFilterClicked(
        id: Long,
        selected: Boolean,
    ) {
        val newValue = !selected
        if (newValue && !prefsInteractor.getAllowMultipleActivityFilters()) {
            activityFilterInteractor.changeSelectedAll(selected = false)
        }
        activityFilterInteractor.changeSelected(id, newValue)
    }
}