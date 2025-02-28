package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsAdditionalViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsAutomatedTrackingMapper
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ActivitySuggestionsParams
import com.example.util.simpletimetracker.navigation.params.screen.ComplexRulesParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsAdditionalViewModelDelegate @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val settingsMapper: SettingsMapper,
    private val settingsAutomatedTrackingMapper: SettingsAutomatedTrackingMapper,
    private val settingsAdditionalViewDataInteractor: SettingsAdditionalViewDataInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) : ViewModelDelegate() {

    val keepScreenOnCheckbox: LiveData<Boolean>
        by lazySuspend { prefsInteractor.getKeepScreenOn() }

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsAdditionalViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.AdditionalCollapse -> onCollapseClick()
            SettingsBlock.AdditionalIgnoreShort -> onIgnoreShortRecordsClicked()
            SettingsBlock.AdditionalShiftStartOfDay -> onStartOfDayClicked()
            SettingsBlock.AdditionalShiftStartOfDayButton -> onStartOfDaySignClicked()
            SettingsBlock.AdditionalAutomatedTracking -> onAutomatedTrackingHelpClick()
            SettingsBlock.AdditionalShowTagSelection -> onShowRecordTagSelectionClicked()
            SettingsBlock.AdditionalCloseAfterOneTag -> onRecordTagSelectionCloseClicked()
            SettingsBlock.AdditionalTagSelectionExcludeActivities -> onRecordTagSelectionExcludeActivitiesClicked()
            SettingsBlock.AdditionalShowCommentInput -> onShowCommentInputClicked()
            SettingsBlock.AdditionalCommentInputExcludeActivities -> onCommentInputExcludeActivitiesClicked()
            SettingsBlock.AdditionalKeepStatisticsRange -> onKeepStatisticsRangeClicked()
            SettingsBlock.AdditionalRetroactiveTrackingMode -> onRetroactiveTrackingModeClicked()
            SettingsBlock.AdditionalSendEvents -> onAutomatedTrackingSendEventsClicked()
            SettingsBlock.AdditionalKeepScreenOn -> onKeepScreenOnClicked()
            SettingsBlock.AdditionalDataEdit -> onDataEditClick()
            SettingsBlock.AdditionalComplexRules -> onComplexRulesClick()
            SettingsBlock.AdditionalActivitySuggestions -> onActivitySuggestionsClick()
            else -> {
                // Do nothing
            }
        }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        when (block) {
            SettingsBlock.DisplayRepeatButtonMode -> onRepeatButtonSelected(position)
            SettingsBlock.AdditionalFirstDayOfWeek -> onFirstDayOfWeekSelected(position)
            else -> {
                // Do nothing
            }
        }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        onDurationSetDelegate(tag, duration)
    }

    fun onDurationDisabled(tag: String?) {
        onDurationDisabledDelegate(tag)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        onDateTimeSetDelegate(timestamp, tag)
    }

    fun onTypesSelected(typeIds: List<Long>, tag: String?) {
        onTypesSelectedDelegate(typeIds, tag)
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    private fun onFirstDayOfWeekSelected(position: Int) {
        val newDayOfWeek = settingsMapper.toDayOfWeek(position)

        delegateScope.launch {
            prefsInteractor.setFirstDayOfWeek(newDayOfWeek)
            externalViewsInteractor.onFirstDayOfWeekChange()
            parent?.updateContent()
        }
    }

    private fun onRepeatButtonSelected(position: Int) {
        val newType = settingsMapper.toRepeatButtonType(position)

        delegateScope.launch {
            prefsInteractor.setRepeatButtonType(newType)
            parent?.updateContent()
        }
    }

    private fun onStartOfDayClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.START_OF_DAY_DIALOG_TAG,
                timestamp = prefsInteractor.getStartOfDayShift(),
                useMilitaryTime = true,
            )
        }
    }

    private fun onStartOfDaySignClicked() {
        delegateScope.launch {
            val newValue = prefsInteractor.getStartOfDayShift() * -1
            prefsInteractor.setStartOfDayShift(newValue)
            externalViewsInteractor.onStartOfDaySignChange()
            parent?.updateContent()
        }
    }

    private fun onKeepStatisticsRangeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getKeepStatisticsRange()
            prefsInteractor.setKeepStatisticsRange(newValue)
            parent?.updateContent()
        }
    }

    private fun onRetroactiveTrackingModeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRetroactiveTrackingMode()
            prefsInteractor.setRetroactiveTrackingMode(newValue)
            parent?.updateContent()
            runningRecordInteractor.getAll().forEach {
                removeRunningRecordMediator.removeWithRecordAdd(it)
            }
            // TODO do not update widgets if there was running records?
            externalViewsInteractor.onRetroactiveTrackingModeChange()
        }
    }

    private fun onIgnoreShortRecordsClicked() {
        delegateScope.launch {
            DurationDialogParams(
                tag = SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG,
                value = DurationDialogParams.Value.DurationSeconds(
                    duration = prefsInteractor.getIgnoreShortRecordsDuration(),
                ),
            ).let(router::navigate)
        }
    }

    private fun onShowRecordTagSelectionClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowRecordTagSelection()
            prefsInteractor.setShowRecordTagSelection(newValue)
            externalViewsInteractor.onShowRecordTagSelectionChange()
            parent?.updateContent()
        }
    }

    private fun onRecordTagSelectionCloseClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            prefsInteractor.setRecordTagSelectionCloseAfterOne(newValue)
            parent?.updateContent()
        }
    }

    private fun onRecordTagSelectionExcludeActivitiesClicked() = delegateScope.launch {
        TypesSelectionDialogParams(
            tag = SettingsViewModel.TAG_EXCLUDE_ACTIVITIES_TYPES_SELECTION,
            title = resourceRepo.getString(
                R.string.record_tag_selection_exclude_activities_title,
            ),
            subtitle = resourceRepo.getString(
                R.string.record_tag_selection_exclude_activities_hint,
            ),
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = prefsInteractor.getRecordTagSelectionExcludeActivities(),
            isMultiSelectAvailable = true,
            idsShouldBeVisible = emptyList(),
            showHints = true,
        ).let(router::navigate)
    }

    private fun onShowCommentInputClicked() = delegateScope.launch {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowCommentInput()
            prefsInteractor.setShowCommentInput(newValue)
            parent?.updateContent()
        }
    }

    private fun onCommentInputExcludeActivitiesClicked() = delegateScope.launch {
        TypesSelectionDialogParams(
            tag = SettingsViewModel.COMMENT_EXCLUDE_ACTIVITIES_TYPES_SELECTION,
            title = resourceRepo.getString(
                R.string.record_tag_selection_exclude_activities_title,
            ),
            subtitle = resourceRepo.getString(
                R.string.record_tag_selection_exclude_activities_hint,
            ),
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = prefsInteractor.getCommentInputExcludeActivities(),
            isMultiSelectAvailable = true,
            idsShouldBeVisible = emptyList(),
            showHints = true,
        ).let(router::navigate)
    }

    private fun onAutomatedTrackingSendEventsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAutomatedTrackingSendEvents()
            prefsInteractor.setAutomatedTrackingSendEvents(newValue)
            parent?.updateContent()
        }
    }

    private fun onKeepScreenOnClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getKeepScreenOn()
            prefsInteractor.setKeepScreenOn(newValue)
            keepScreenOnCheckbox.set(newValue)
            parent?.updateContent()
        }
    }

    private fun onDataEditClick() {
        router.navigate(DataEditParams)
    }

    private fun onComplexRulesClick() {
        router.navigate(ComplexRulesParams)
    }

    private fun onActivitySuggestionsClick() {
        router.navigate(ActivitySuggestionsParams)
    }

    private fun onAutomatedTrackingHelpClick() {
        delegateScope.launch {
            val isDarkTheme = prefsInteractor.getDarkMode()
            settingsAutomatedTrackingMapper.toAutomatedTrackingHelpDialog(isDarkTheme)
                .let(router::navigate)
        }
    }

    private fun onDurationSetDelegate(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(duration)
                parent?.updateContent()
            }
        }
    }

    private fun onDurationDisabledDelegate(tag: String?) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_RECORDS_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortRecordsDuration(0)
                parent?.updateContent()
            }
        }
    }

    private fun onDateTimeSetDelegate(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.START_OF_DAY_DIALOG_TAG -> {
                val wasPositive = prefsInteractor.getStartOfDayShift() >= 0
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive)
                prefsInteractor.setStartOfDayShift(newValue)
                externalViewsInteractor.onStartOfDayChange()
                parent?.updateContent()
            }
        }
    }

    private fun onTypesSelectedDelegate(typeIds: List<Long>, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.TAG_EXCLUDE_ACTIVITIES_TYPES_SELECTION -> {
                prefsInteractor.setRecordTagSelectionExcludeActivities(typeIds)
            }
            SettingsViewModel.COMMENT_EXCLUDE_ACTIVITIES_TYPES_SELECTION -> {
                prefsInteractor.setCommentInputExcludeActivities(typeIds)
            }
        }
    }
}