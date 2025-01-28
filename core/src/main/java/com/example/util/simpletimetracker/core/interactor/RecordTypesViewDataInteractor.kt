package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import javax.inject.Inject

class RecordTypesViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) {

    suspend fun getTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .takeUnless { it.isEmpty() }
            ?.map {
                recordTypeViewDataMapper.map(
                    recordType = it,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                )
            }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}