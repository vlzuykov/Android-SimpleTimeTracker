package com.example.util.simpletimetracker.feature_running_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.GetChangeRecordNavigationParamsInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ChangeSelectedActivityFilterMediator
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_running_records.interactor.RunningRecordsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DefaultTypesSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.PomodoroParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunningRecordsViewModel @Inject constructor(
    private val router: Router,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordRepeatInteractor: RecordRepeatInteractor,
    private val runningRecordsViewDataInteractor: RunningRecordsViewDataInteractor,
    private val changeSelectedActivityFilterMediator: ChangeSelectedActivityFilterMediator,
    private val prefsInteractor: PrefsInteractor,
    private val updateRunningRecordFromChangeScreenInteractor: UpdateRunningRecordFromChangeScreenInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val getChangeRecordNavigationParamsInteractor: GetChangeRecordNavigationParamsInteractor,
) : ViewModel() {

    val runningRecords: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData()))
    }
    val resetScreen: SingleLiveEvent<Unit> =
        SingleLiveEvent()
    val previewUpdate: SingleLiveEvent<UpdateRunningRecordFromChangeScreenInteractor.Update> =
        SingleLiveEvent()

    private var timerJob: Job? = null
    private var completeTypeJob: Job? = null
    private var completeTypeIds: Set<Long> = emptySet()

    init {
        subscribeToUpdates()
    }

    fun onRecordTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            val runningRecord = runningRecordInteractor.get(item.id)

            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                val wasStarted = addRunningRecordMediator.tryStartTimer(
                    typeId = item.id,
                    onNeedToShowTagSelection = { showTagSelection(item.id, it) },
                )
                if (wasStarted) {
                    onRecordTypeWithDefaultDurationClick(item.id)
                }
            }
            updateRunningRecords()
        }
    }

    fun onRecordTypeLongClick(item: RecordTypeViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeRecordTypeParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                sizePreview = ChangeRecordTypeParams.SizePreview(
                    width = item.width,
                    height = item.height,
                    asRow = item.asRow,
                ),
                preview = ChangeRecordTypeParams.Change.Preview(
                    name = item.name,
                    iconId = item.iconId.toParams(),
                    color = item.color,
                ),
            ),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onSpecialRecordTypeClick(item: RunningRecordTypeSpecialViewData) {
        when (item.type) {
            is RunningRecordTypeSpecialViewData.Type.Add -> {
                router.navigate(
                    data = ChangeRecordTypeParams.New(
                        sizePreview = ChangeRecordTypeParams.SizePreview(
                            width = item.width,
                            height = item.height,
                            asRow = item.asRow,
                        ),
                    ),
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Default -> {
                router.navigate(
                    data = DefaultTypesSelectionDialogParams,
                )
            }
            is RunningRecordTypeSpecialViewData.Type.Repeat -> viewModelScope.launch {
                recordRepeatInteractor.repeat()
            }
            is RunningRecordTypeSpecialViewData.Type.Pomodoro -> {
                router.navigate(PomodoroParams)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onRunningRecordClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) {
        viewModelScope.launch {
            runningRecordInteractor.get(item.id)
                ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
            updateRunningRecords()
        }
    }

    fun onRunningRecordLongClick(
        item: RunningRecordViewData,
        sharedElements: Pair<Any, String>,
    ) = viewModelScope.launch {
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        if (item.id == UNTRACKED_ITEM_ID) {
            // Currently possible in retroactive mode.
            // Open running record as an untracked record.
            val timeEndedTimestamp = System.currentTimeMillis()
            val recordItem = RecordViewData.Untracked(
                timeStartedTimestamp = item.timeStartedTimestamp,
                timeEndedTimestamp = timeEndedTimestamp,
                name = item.name,
                timeStarted = item.timeStarted,
                timeFinished = "",
                duration = item.timer,
                iconId = item.iconId,
                color = item.color,
            )
            val params = getChangeRecordNavigationParamsInteractor.execute(
                item = recordItem,
                from = ChangeRecordParams.From.Records,
                shift = 0,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                sharedElements = null,
            )
            router.navigate(
                data = ChangeRecordFromMainParams(params = params),
            )
        } else {
            val params = getChangeRecordNavigationParamsInteractor.execute(
                item = item,
                from = ChangeRunningRecordParams.From.RunningRecords,
                useMilitaryTimeFormat = useMilitaryTimeFormat,
                showSeconds = showSeconds,
                sharedElements = sharedElements,
            )
            router.navigate(
                data = ChangeRunningRecordFromMainParams(params = params),
                sharedElements = sharedElements.let(::mapOf),
            )
        }
    }

    fun onActivityFilterClick(item: ActivityFilterViewData) {
        viewModelScope.launch {
            changeSelectedActivityFilterMediator.onFilterClicked(item.id, item.selected)
            updateRunningRecords()
        }
    }

    fun onActivityFilterLongClick(item: ActivityFilterViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeActivityFilterParams.Change(
                transitionName = sharedElements.second,
                id = item.id,
                preview = ChangeActivityFilterParams.Change.Preview(
                    name = item.name,
                    color = item.color,
                ),
            ),
            sharedElements = sharedElements.let(::mapOf),
        )
    }

    fun onAddActivityFilterClick() = viewModelScope.launch {
        router.navigate(
            data = ChangeActivityFilterParams.New,
        )
    }

    fun onVisible() {
        startUpdate()
    }

    fun onHidden() {
        stopUpdate()
    }

    fun onTagSelected() {
        updateRunningRecords()
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (tab is NavigationTab.RunningRecords) {
            resetScreen.set(Unit)
        }
    }

    private suspend fun onRecordTypeWithDefaultDurationClick(typeId: Long) {
        val defaultDuration = recordTypeInteractor.get(typeId)?.defaultDuration
        if (defaultDuration.orZero() <= 0L) return

        completeTypeIds = completeTypeIds + typeId
        completeTypeJob?.cancel()
        completeTypeJob = viewModelScope.launch {
            delay(COMPLETE_TYPE_ANIMATION_MS)
            completeTypeIds = completeTypeIds - typeId
            updateRunningRecords()
        }
    }

    private fun showTagSelection(
        typeId: Long,
        result: RecordDataSelectionDialogResult,
    ) {
        router.navigate(RecordTagSelectionParams(typeId, result.toParams()))
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            updateRunningRecordFromChangeScreenInteractor.dataUpdated.collect {
                onUpdateReceived(it)
            }
        }
    }

    private fun onUpdateReceived(
        update: UpdateRunningRecordFromChangeScreenInteractor.Update,
    ) {
        previewUpdate.set(update)
    }

    private fun updateRunningRecords() = viewModelScope.launch {
        val data = loadRunningRecordsViewData()
        runningRecords.set(data)
    }

    private suspend fun loadRunningRecordsViewData(): List<ViewHolderType> {
        return runningRecordsViewDataInteractor.getViewData(
            completeTypeIds = completeTypeIds,
        )
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateRunningRecords()
                delay(TIMER_UPDATE_MS)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE_MS = 1000L
        private const val COMPLETE_TYPE_ANIMATION_MS = 1000L
    }
}
