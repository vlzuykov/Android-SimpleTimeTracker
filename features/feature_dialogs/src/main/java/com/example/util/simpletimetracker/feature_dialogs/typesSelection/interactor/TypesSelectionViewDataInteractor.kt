package com.example.util.simpletimetracker.feature_dialogs.typesSelection.interactor

import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.model.TypesSelectionCacheHolder
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import javax.inject.Inject

class TypesSelectionViewDataInteractor @Inject constructor(
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun loadCache(
        extra: TypesSelectionDialogParams,
        types: List<RecordType>,
    ): List<TypesSelectionCacheHolder> {
        val extraType = extra.type
        return when (extraType) {
            is TypesSelectionDialogParams.Type.Activity -> {
                types.filter {
                    !it.hidden || it.id in extra.idsShouldBeVisible
                }.map(TypesSelectionCacheHolder::Type)
            }
            is TypesSelectionDialogParams.Type.Tag -> {
                val tags = when (extraType) {
                    is TypesSelectionDialogParams.Type.Tag.All -> {
                        recordTagInteractor.getAll()
                    }
                    is TypesSelectionDialogParams.Type.Tag.ByType -> {
                        getSelectableTagsInteractor.execute(extraType.typeId)
                    }
                }
                tags.filter {
                    !it.archived || it.id in extra.idsShouldBeVisible
                }.map(TypesSelectionCacheHolder::Tag)
            }
        }
    }

    suspend fun getViewData(
        extra: TypesSelectionDialogParams,
        types: List<RecordType>,
        dataIdsSelected: List<Long>,
        viewDataCache: List<TypesSelectionCacheHolder>,
    ): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val typesMap = types.associateBy(RecordType::id)

        fun map(type: TypesSelectionCacheHolder): ViewHolderType {
            return when (type) {
                is TypesSelectionCacheHolder.Type -> {
                    recordTypeViewDataMapper.map(
                        recordType = type.data,
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                        checkState = GoalCheckmarkView.CheckState.HIDDEN,
                        isComplete = false,
                    )
                }
                is TypesSelectionCacheHolder.Tag -> {
                    categoryViewDataMapper.mapRecordTag(
                        tag = type.data,
                        type = typesMap[type.data.iconColorSource],
                        isDarkTheme = isDarkTheme,
                    )
                }
            }
        }

        val selected = viewDataCache
            .filter { it.id in dataIdsSelected }
            .map(::map)
        val available = viewDataCache
            .filter { it.id !in dataIdsSelected }
            .map(::map)

        val result = mutableListOf<ViewHolderType>()

        if (viewDataCache.isEmpty()) {
            val message = when (extra.type) {
                is TypesSelectionDialogParams.Type.Activity ->
                    R.string.record_types_empty
                is TypesSelectionDialogParams.Type.Tag ->
                    R.string.chart_filter_categories_empty
            }.let(resourceRepo::getString)
            result += EmptyViewData(message = message)
            return result
        }

        if (selected.isNotEmpty() && extra.showHints) {
            result += InfoViewData(resourceRepo.getString(R.string.something_selected))
        }
        result += selected
        if (selected.isEmpty() && extra.showHints) {
            result += InfoViewData(resourceRepo.getString(R.string.nothing_selected))
        }
        if (available.isNotEmpty() && extra.showHints) {
            result += DividerViewData(0)
        }
        result += available

        return result
    }
}