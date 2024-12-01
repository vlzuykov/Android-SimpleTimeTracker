package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor.RecordQuickActionsInteractor
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor.RecordQuickActionsViewDataInteractor
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordQuickActionsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val recordQuickActionsViewDataInteractor: RecordQuickActionsViewDataInteractor,
    private val recordQuickActionsInteractor: RecordQuickActionsInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val recordActionContinueMediator: RecordActionContinueMediator,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
) : BaseViewModel() {

    lateinit var extra: RecordQuickActionsParams

    val state: LiveData<RecordQuickActionsState> by lazySuspend { loadState() }
    val actionComplete: LiveData<Unit> = SingleLiveEvent<Unit>()

    private var buttonsBlocked: Boolean = false

    fun onButtonClick(block: RecordQuickActionsButton) {
        when (block) {
            RecordQuickActionsButton.DELETE ->
                onButtonClick(onProceed = ::onDelete)
            RecordQuickActionsButton.STATISTICS ->
                onButtonClick(onProceed = ::goToStatistics)
            RecordQuickActionsButton.CONTINUE ->
                onButtonClick(canProceed = ::canContinue, onProceed = ::onContinue)
            RecordQuickActionsButton.REPEAT ->
                onButtonClick(onProceed = ::onRepeat)
            RecordQuickActionsButton.DUPLICATE ->
                onButtonClick(onProceed = ::onDuplicate)
            RecordQuickActionsButton.MERGE ->
                onButtonClick(onProceed = ::onMerge)
            RecordQuickActionsButton.STOP ->
                onButtonClick(onProceed = ::onStop)
            RecordQuickActionsButton.CHANGE_ACTIVITY ->
                onButtonClick(delayBlock = true, onProceed = ::onChangeActivity)
        }
    }

    private suspend fun goToStatistics() {
        val params = extra.type ?: return
        val preview = extra.preview ?: return
        val itemId = when (params) {
            is Type.RecordTracked -> recordInteractor.get(params.id)?.typeId ?: return
            is Type.RecordUntracked -> UNTRACKED_ITEM_ID
            is Type.RecordRunning -> params.id
        }

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            sharedElements = emptyMap(),
            itemId = itemId,
            itemName = preview.name,
            itemIcon = preview.iconId.toViewData(),
            itemColor = preview.color,
        )
    }

    private suspend fun onDelete() {
        val params = extra.type ?: return
        when (params) {
            is Type.RecordTracked -> {
                // Removal handled in separate viewModel.
                router.back()
            }
            is Type.RecordUntracked -> {
                // Do nothing, shouldn't be possible.
            }
            is Type.RecordRunning -> {
                removeRunningRecordMediator.remove(params.id)
                showMessage(R.string.change_running_record_removed)
                exit()
            }
        }
    }

    private suspend fun canContinue(): Boolean {
        val record = getTrackedRecord() ?: return false
        // Can't continue future record
        return if (record.timeStarted > System.currentTimeMillis()) {
            showMessage(R.string.cannot_be_in_the_future)
            false
        } else {
            true
        }
    }

    private suspend fun onContinue() {
        val record = getTrackedRecord() ?: return
        recordActionContinueMediator.execute(
            recordId = record.id,
            typeId = record.typeId,
            timeStarted = record.timeStarted,
            comment = record.comment,
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onRepeat() {
        val record = getTrackedRecord() ?: return
        recordActionRepeatMediator.execute(
            typeId = record.typeId,
            comment = record.comment,
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onDuplicate() {
        val record = getTrackedRecord() ?: return
        recordActionDuplicateMediator.execute(
            typeId = record.typeId,
            timeStarted = record.timeStarted,
            timeEnded = record.timeEnded,
            comment = record.comment,
            tagIds = record.tagIds,
        )
        exit()
    }

    private suspend fun onMerge() {
        val record = extra.type as? Type.RecordUntracked ?: return
        val prevRecord = recordInteractor.getPrev(timeStarted = record.timeStarted)
        recordActionMergeMediator.execute(
            prevRecord = prevRecord,
            newTimeEnded = record.timeEnded,
            onMergeComplete = ::exit,
        )
    }

    private suspend fun onStop() {
        val record = extra.type as? Type.RecordRunning ?: return
        runningRecordInteractor.get(record.id)
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
        exit()
    }

    private fun onChangeActivity() {
        TypesSelectionDialogParams(
            tag = RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG,
            title = resourceRepo.getString(R.string.change_record_message_choose_type),
            subtitle = "",
            type = TypesSelectionDialogParams.Type.Activity,
            selectedTypeIds = emptyList(),
            isMultiSelectAvailable = false,
            idsShouldBeVisible = emptyList(),
            showHints = false,
        ).let(router::navigate)
    }

    fun onTypesSelected(typeIds: List<Long>, tag: String?) = viewModelScope.launch {
        if (tag != RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG) return@launch

        buttonsBlocked = true
        val typeId = typeIds.firstOrNull() ?: return@launch
        val params = extra.type ?: return@launch
        recordQuickActionsInteractor.changeType(params, typeId)
        exit()
    }

    private suspend fun getTrackedRecord(): Record? {
        val recordId = (extra.type as? Type.RecordTracked)?.id
            ?: return null
        return recordInteractor.get(recordId)
    }

    private fun onButtonClick(
        delayBlock: Boolean = false,
        canProceed: suspend () -> Boolean = { true },
        onProceed: suspend () -> Unit,
    ) {
        viewModelScope.launch {
            if (!canProceed()) return@launch
            if (buttonsBlocked) return@launch
            if (!delayBlock) buttonsBlocked = true
            onProceed()
        }
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            inDialog = true,
        )
        router.show(params)
    }

    private fun exit() {
        actionComplete.set(Unit)
        router.back()
    }

    private suspend fun loadState(): RecordQuickActionsState {
        return recordQuickActionsViewDataInteractor.getViewData(extra)
    }

    companion object {
        private const val RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG = "RECORD_QUICK_ACTIONS_TYPE_SELECTION_TAG"
    }
}
