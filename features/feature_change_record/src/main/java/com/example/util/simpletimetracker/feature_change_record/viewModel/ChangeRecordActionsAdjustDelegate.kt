package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRecordMediator
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintAccentViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeDoublePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.model.TimeAdjustmentState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordAdjustState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordActionsAdjustDelegate @Inject constructor(
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val removeRecordMediator: RemoveRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordInteractor: RecordInteractor,
    private val timeMapper: TimeMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModelDelegate(),
    ChangeRecordActionsSubDelegate<ChangeRecordActionsAdjustDelegate.Parent> {

    var timeChangeAdjustmentState: TimeAdjustmentState = TimeAdjustmentState.TIME_STARTED

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()
    private var recordsUnmarkedFromAdjustment: List<Long> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadViewData()
        parent?.update()
    }

    fun onAdjustTimeStartedClick() {
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_STARTED,
            other = TimeAdjustmentState.TIME_ENDED,
        )
    }

    fun onAdjustTimeEndedClick() {
        updateAdjustTimeState(
            clicked = TimeAdjustmentState.TIME_ENDED,
            other = TimeAdjustmentState.TIME_STARTED,
        )
    }

    fun onChangePreviewCheckClick(item: ChangeRecordChangePreviewViewData) {
        delegateScope.launch {
            recordsUnmarkedFromAdjustment = recordsUnmarkedFromAdjustment
                .toMutableList()
                .apply { addOrRemove(item.id) }
            updateViewData()
        }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val showSeconds = prefsInteractor.getShowSeconds()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        val hintText = if (params.isTimeEndedAvailable) {
            R.string.change_record_change_adjacent_records
        } else {
            R.string.change_record_change_prev_record
        }.let(resourceRepo::getString)
        result += HintViewData(hintText)
        val state = loadViewData(
            recordId = params.originalRecordId,
            adjustNextRecordAvailable = params.adjustNextRecordAvailable,
            newTypeId = params.newTypeId,
            newTimeStarted = params.newTimeStarted,
            adjustPreviewTimeEnded = params.adjustPreviewTimeEnded,
            originalTypeId = params.originalTypeId,
            originalTimeStarted = params.originalTimeStarted,
            originalTimeEnded = params.adjustPreviewOriginalTimeEnded,
            showTimeEnded = params.showTimeEndedOnAdjustPreview,
        )
        val previewData = state.currentData
        result += ChangeRecordChangePreviewViewData(
            id = previewData.id,
            before = previewData.before,
            after = previewData.after,
            isChecked = false,
            marginTopDp = 0,
            isRemoveVisible = false,
            isCheckVisible = false,
            isCompareVisible = true,
        )
        result += ChangeRecordTimeDoublePreviewViewData(
            block = ChangeRecordActionsBlock.AdjustTimePreview,
            dateTimeStarted = timeMapper.getFormattedDateTime(
                time = params.newTimeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ).time,
            dateTimeFinished = timeMapper.getFormattedDateTime(
                time = params.adjustPreviewTimeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ).time,
            isTimeEndedAvailable = params.isTimeEndedAvailable,
            state = timeChangeAdjustmentState,
        )
        if (timeChangeAdjustmentState != TimeAdjustmentState.HIDDEN) {
            result += ChangeRecordTimeAdjustmentViewData(
                block = ChangeRecordActionsBlock.AdjustTimeAdjustment,
                items = loadTimeAdjustmentItems(),
            )
        }
        result += state.changesPreview
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.ADJUST,
            isEnabled = params.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    suspend fun onAdjustClickDelegate() {
        val params = parent?.getViewDataParams() ?: return

        val adjacentRecords = getAdjacentRecords(
            recordId = params.originalRecordId,
            newTimeStarted = params.newTimeStarted,
            newTimeEnded = params.newTimeEnded,
            adjustNextRecordAvailable = params.adjustNextRecordAvailable,
        )

        adjacentRecords.previous
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { prevRecord ->
                getChangedPrevRecord(
                    record = prevRecord,
                    newTimeStarted = params.newTimeStarted,
                ).let { addRecordMediator.add(it) }
            }

        adjacentRecords.overlapped
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { overlappedRecord ->
                removeRecordMediator.remove(
                    recordId = overlappedRecord.id,
                    typeId = overlappedRecord.typeId,
                )
            }

        adjacentRecords.next
            .filter { it.id !in recordsUnmarkedFromAdjustment }
            .forEach { nextRecord ->
                getChangedNextRecord(
                    record = nextRecord,
                    newTimeEnded = params.newTimeEnded,
                ).let { addRecordMediator.add(it) }
            }

        parent?.onAdjustComplete()
    }

    private suspend fun loadViewData(
        recordId: Long,
        adjustNextRecordAvailable: Boolean,
        newTypeId: Long,
        newTimeStarted: Long,
        adjustPreviewTimeEnded: Long,
        originalTypeId: Long,
        originalTimeStarted: Long,
        originalTimeEnded: Long,
        showTimeEnded: Boolean,
    ): ChangeRecordAdjustState {
        val adjacentRecords = getAdjacentRecords(
            recordId = recordId,
            newTimeStarted = newTimeStarted,
            newTimeEnded = adjustPreviewTimeEnded,
            adjustNextRecordAvailable = adjustNextRecordAvailable,
        )

        val currentData = loadAdjustPreviewViewData(
            record = Record(
                id = 0,
                typeId = originalTypeId,
                timeStarted = originalTimeStarted,
                timeEnded = originalTimeEnded,
                comment = "",
            ),
            changedRecord = Record(
                id = 0,
                typeId = newTypeId,
                timeStarted = newTimeStarted,
                timeEnded = adjustPreviewTimeEnded,
                comment = "",
            ),
            showTimeEnded = showTimeEnded,
        )

        val prevData = adjacentRecords.previous.map { prevRecord ->
            loadAdjustPreviewViewData(
                record = prevRecord,
                changedRecord = getChangedPrevRecord(
                    record = prevRecord,
                    newTimeStarted = newTimeStarted,
                ),
                showTimeEnded = true,
            )
        }

        val overlappedData = adjacentRecords.overlapped.map { overlappedRecord ->
            loadAdjustPreviewViewData(
                record = overlappedRecord,
                changedRecord = overlappedRecord,
                showTimeEnded = true,
            )
        }

        val nextData = adjacentRecords.next.map { nextRecord ->
            loadAdjustPreviewViewData(
                record = nextRecord,
                changedRecord = getChangedNextRecord(
                    record = nextRecord,
                    newTimeEnded = adjustPreviewTimeEnded,
                ),
                showTimeEnded = true,
            )
        }

        val viewData = mutableListOf<ViewHolderType>()

        fun mapItem(
            data: ChangeRecordPreview,
            isRemoveVisible: Boolean = false,
        ): ViewHolderType {
            return ChangeRecordChangePreviewViewData(
                id = data.id,
                before = data.before,
                after = data.after,
                isChecked = data.id !in recordsUnmarkedFromAdjustment,
                marginTopDp = 0,
                isRemoveVisible = isRemoveVisible,
                isCheckVisible = true,
                isCompareVisible = true,
            )
        }

        if (nextData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_next),
            )
            viewData += nextData.map(::mapItem)
        }

        if (overlappedData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_overlapped),
                paddingBottom = 0,
            )
            viewData += HintAccentViewData(
                resourceRepo.getString(R.string.change_record_change_overlapped_hint),
                paddingTop = 0,
            )
            viewData += overlappedData.map { mapItem(it, isRemoveVisible = true) }
        }

        if (prevData.isNotEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.change_record_change_prev),
            )
            viewData += prevData.map(::mapItem)
        }

        if (viewData.isEmpty()) {
            viewData += HintViewData(
                resourceRepo.getString(R.string.no_records_exist),
            )
        }

        return ChangeRecordAdjustState(
            currentData = currentData,
            changesPreview = viewData,
        )
    }

    private suspend fun getAdjacentRecords(
        recordId: Long,
        newTimeStarted: Long,
        newTimeEnded: Long,
        adjustNextRecordAvailable: Boolean,
    ): AdjacentRecords {
        suspend fun getNext(): List<Record> {
            return recordInteractor.getAllNext(newTimeEnded)
        }

        val recordRange = Range(timeStarted = newTimeStarted, timeEnded = newTimeEnded)
        val adjacentRecords = recordInteractor.getFromRange(recordRange)
            .sortedByDescending { it.timeStarted }

        val previousRecords = adjacentRecords
            .filter { it.timeStarted < newTimeStarted && it.timeEnded <= newTimeEnded }
            .ifEmpty { recordInteractor.getAllPrev(newTimeStarted) }
            .filter { it.id != recordId }
        val overlappedRecords = adjacentRecords
            .filter { it.timeStarted >= newTimeStarted && it.timeEnded <= newTimeEnded }
            .filter { it.id != recordId }
        val nextRecords = adjacentRecords
            .filter { it.timeStarted >= newTimeStarted && it.timeEnded > newTimeEnded }
            .ifEmpty { if (adjustNextRecordAvailable) getNext() else emptyList() }
            .takeIf { adjustNextRecordAvailable }
            .orEmpty()
            .filter { it.id != recordId }

        return AdjacentRecords(
            previous = previousRecords,
            overlapped = overlappedRecords,
            next = nextRecords,
        )
    }

    private suspend fun loadAdjustPreviewViewData(
        record: Record,
        changedRecord: Record,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview {
        val dateTimeFieldState = ChangeRecordDateTimeFieldsState(
            start = ChangeRecordDateTimeFieldsState.State.DateTime,
            end = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(record, dateTimeFieldState)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord, dateTimeFieldState)

        return ChangeRecordPreview(
            id = record.id,
            before = changeRecordViewDataMapper.mapSimple(
                preview = previousRecordPreview,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = false,
                timeEndedChanged = false,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = changedRecordPreview,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = changedRecord.timeStarted != record.timeStarted,
                timeEndedChanged = changedRecord.timeEnded != record.timeEnded,
            ),
        )
    }

    private fun getChangedPrevRecord(
        record: Record,
        newTimeStarted: Long,
    ): Record {
        return record.let {
            it.copy(
                timeStarted = it.timeStarted.coerceAtMost(newTimeStarted),
                timeEnded = newTimeStarted,
            )
        }
    }

    private fun getChangedNextRecord(
        record: Record,
        newTimeEnded: Long,
    ): Record {
        return record.let {
            it.copy(
                timeStarted = newTimeEnded,
                timeEnded = it.timeEnded.coerceAtLeast(newTimeEnded),
            )
        }
    }

    private fun updateAdjustTimeState(
        clicked: TimeAdjustmentState,
        other: TimeAdjustmentState,
    ) = delegateScope.launch {
        when (timeChangeAdjustmentState) {
            TimeAdjustmentState.HIDDEN -> {
                timeChangeAdjustmentState = clicked
                updateViewData()
            }
            clicked -> {
                timeChangeAdjustmentState = TimeAdjustmentState.HIDDEN
                updateViewData()
            }
            other -> delegateScope.launch {
                timeChangeAdjustmentState = TimeAdjustmentState.HIDDEN
                updateViewData()
                delay(300)
                timeChangeAdjustmentState = clicked
                updateViewData()
            }
            else -> {
                // Do nothing
            }
        }
    }

    private fun loadTimeAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems(
            dateTimeFieldState = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
    }

    private data class AdjacentRecords(
        val previous: List<Record>,
        val overlapped: List<Record>,
        val next: List<Record>,
    )

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onAdjustComplete()

        data class ViewDataParams(
            val originalRecordId: Long,
            val adjustNextRecordAvailable: Boolean,
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val adjustPreviewTimeEnded: Long,
            val originalTypeId: Long,
            val originalTimeStarted: Long,
            val adjustPreviewOriginalTimeEnded: Long,
            val showTimeEndedOnAdjustPreview: Boolean,
            val isTimeEndedAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}