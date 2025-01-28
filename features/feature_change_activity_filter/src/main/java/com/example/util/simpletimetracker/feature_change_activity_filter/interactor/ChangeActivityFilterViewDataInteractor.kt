package com.example.util.simpletimetracker.feature_change_activity_filter.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterTypesViewData
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import javax.inject.Inject

class ChangeActivityFilterViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    suspend fun getTypesViewData(
        type: ActivityFilter.Type,
        selectedIds: Set<Long>,
    ): ChangeActivityFilterTypesViewData {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val data = when (type) {
            is ActivityFilter.Type.Activity -> {
                recordTypeInteractor.getAll()
                    .filter { !it.hidden }
                    .map {
                        it.id to recordTypeViewDataMapper.map(
                            recordType = it,
                            numberOfCards = numberOfCards,
                            isDarkTheme = isDarkTheme,
                            checkState = GoalCheckmarkView.CheckState.HIDDEN,
                            isComplete = false,
                        )
                    }
            }
            is ActivityFilter.Type.Category -> {
                categoryInteractor.getAll()
                    .map {
                        it.id to categoryViewDataMapper.mapCategory(
                            category = it,
                            isDarkTheme = isDarkTheme,
                        )
                    }
            }
        }

        return if (data.isNotEmpty()) {
            val selected = data.filter { it.first in selectedIds }.map { it.second }
            val available = data.filter { it.first !in selectedIds }.map { it.second }
            val viewData = mutableListOf<ViewHolderType>()
            commonViewDataMapper.mapSelectedHint(
                isEmpty = selected.isEmpty(),
            ).let(viewData::add)
            selected.let(viewData::addAll)
            DividerViewData(1)
                .takeUnless { available.isEmpty() }
                ?.let(viewData::add)
            available.let(viewData::addAll)

            ChangeActivityFilterTypesViewData(
                selectedCount = selected.size,
                viewData = viewData,
            )
        } else {
            ChangeActivityFilterTypesViewData(
                selectedCount = 0,
                viewData = when (type) {
                    is ActivityFilter.Type.Activity -> {
                        recordTypeViewDataMapper.mapToEmpty()
                    }
                    is ActivityFilter.Type.Category -> {
                        listOf(categoryViewDataMapper.mapToCategoriesEmpty())
                    }
                },
            )
        }
    }
}