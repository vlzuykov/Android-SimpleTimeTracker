package com.example.util.simpletimetracker.feature_suggestions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_suggestions.R
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionSpecialViewData
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionsButtonViewData
import com.example.util.simpletimetracker.feature_suggestions.interactor.ActivitySuggestionsCalculateInteractor
import com.example.util.simpletimetracker.feature_suggestions.interactor.ActivitySuggestionsViewDataInteractor
import com.example.util.simpletimetracker.feature_suggestions.model.ActivitySuggestionModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitySuggestionsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val activitySuggestionsViewDataInteractor: ActivitySuggestionsViewDataInteractor,
    private val activitySuggestionsCalculateInteractor: ActivitySuggestionsCalculateInteractor,
) : BaseViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazySuspend {
        listOf(LoaderViewData()).also { updateViewData() }
    }

    private var suggestions: List<ActivitySuggestionModel> = emptyList()
    private var selectingSuggestionsForTypeId: Long = 0L

    fun onTypesSelected(typeIds: List<Long>, tag: String?) = viewModelScope.launch {
        when (tag) {
            ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG -> {
                suggestions = typeIds.map { typeId ->
                    ActivitySuggestionModel(
                        typeId = typeId,
                        suggestions = suggestions.firstOrNull {
                            it.typeId == typeId
                        }?.suggestions.orEmpty(),
                    )
                }
                updateViewData()
            }
            ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG -> {
                suggestions = suggestions.map { suggestion ->
                    val newSuggestions = if (
                        suggestion.typeId == selectingSuggestionsForTypeId
                    ) {
                        typeIds
                    } else {
                        suggestion.suggestions
                    }
                    ActivitySuggestionModel(
                        typeId = suggestion.typeId,
                        suggestions = newSuggestions,
                    )
                }
                updateViewData()
            }
        }
    }

    fun onSpecialSuggestionClick(item: ActivitySuggestionSpecialViewData) {
        when (item.id.type) {
            is ActivitySuggestionSpecialViewData.Type.Add -> {
                val forTypeId = item.id.forTypeId
                selectingSuggestionsForTypeId = forTypeId
                TypesSelectionDialogParams(
                    tag = ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG,
                    title = resourceRepo.getString(R.string.change_record_message_choose_type),
                    subtitle = "", // TODO SUG add hint
                    type = TypesSelectionDialogParams.Type.Activity,
                    selectedTypeIds = suggestions.firstOrNull {
                        it.typeId == forTypeId
                    }?.suggestions.orEmpty(),
                    isMultiSelectAvailable = true,
                    idsShouldBeVisible = emptyList(),
                    showHints = true,
                ).let(router::navigate)
            }
            is ActivitySuggestionSpecialViewData.Type.Calculate -> viewModelScope.launch {
                val forTypeId = item.id.forTypeId
                selectingSuggestionsForTypeId = forTypeId
                val newData = activitySuggestionsCalculateInteractor
                    .execute(listOf(forTypeId))
                    .firstOrNull { it.typeId == forTypeId }
                    ?.suggestions
                    .orEmpty()
                // TODO SUG do better
                onTypesSelected(newData, ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG)
            }
        }
    }

    fun onItemButtonClick(viewData: ActivitySuggestionsButtonViewData) {
        when (viewData.block) {
            ActivitySuggestionsButtonViewData.Block.ADD -> {
                TypesSelectionDialogParams(
                    tag = ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG,
                    title = resourceRepo.getString(R.string.change_record_message_choose_type),
                    subtitle = "Suggestions will be shown for selected activities", // TODO SUG
                    type = TypesSelectionDialogParams.Type.Activity,
                    selectedTypeIds = suggestions.map { it.typeId },
                    isMultiSelectAvailable = true,
                    idsShouldBeVisible = emptyList(),
                    showHints = true,
                ).let(router::navigate)
            }
            ActivitySuggestionsButtonViewData.Block.CALCULATE -> viewModelScope.launch {
                val selectedTypeIds = suggestions.map { it.typeId }
                suggestions = activitySuggestionsCalculateInteractor.execute(selectedTypeIds)
                updateViewData()
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            // TODO SUG
            router.back()
        }
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return activitySuggestionsViewDataInteractor.getViewData(
            suggestions = suggestions,
        )
    }

    companion object {
        private const val ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG =
            "ACTIVITY_SUGGESTIONS_TYPE_SELECTION_TAG"
        private const val ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG =
            "ACTIVITY_SUGGESTIONS_SUGGESTION_SELECTION_TAG"
    }
}
