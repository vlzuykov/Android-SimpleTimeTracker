package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionMergeMediator
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsMergeDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val recordActionMergeMediator: RecordActionMergeMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsMergeDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

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

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.mergeAvailable) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        val previewData = loadMergePreviewViewData(
            prevRecord = params.prevRecord,
            newTimeEnded = params.newTimeEnded,
        )
        if (previewData != null) {
            result += HintViewData(
                text = resourceRepo.getString(R.string.change_record_merge_hint),
            )
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
            result += changeRecordViewDataMapper.mapRecordActionButton(
                action = RecordQuickAction.MERGE,
                isEnabled = params.isButtonEnabled,
                isDarkTheme = isDarkTheme,
            )
        }
        return result
    }

    suspend fun onMergeClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionMergeMediator.execute(
            prevRecord = params.prevRecord,
            newTimeEnded = params.newTimeEnded,
            onMergeComplete = { router.back() },
        )
    }

    private fun getChangedRecord(
        previousRecord: Record,
        newTimeEnded: Long,
    ): Record {
        return previousRecord.copy(
            timeEnded = newTimeEnded,
        )
    }

    private suspend fun loadMergePreviewViewData(
        prevRecord: Record?,
        newTimeEnded: Long,
    ): ChangeRecordPreview? {
        if (prevRecord == null) return null

        val dateTimeFieldState = ChangeRecordDateTimeFieldsState(
            start = ChangeRecordDateTimeFieldsState.State.DateTime,
            end = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
        val changedRecord = getChangedRecord(prevRecord, newTimeEnded)
        val previousRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(prevRecord, dateTimeFieldState)
        val changedRecordPreview = changeRecordViewDataInteractor
            .getPreviewViewData(changedRecord, dateTimeFieldState)

        return ChangeRecordPreview(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = previousRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = false,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = changedRecordPreview,
                showTimeEnded = true,
                timeStartedChanged = changedRecord.timeStarted != prevRecord.timeStarted,
                timeEndedChanged = changedRecord.timeEnded != prevRecord.timeEnded,
            ),
        )
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()

        data class ViewDataParams(
            val mergeAvailable: Boolean,
            val prevRecord: Record?,
            val newTimeEnded: Long,
            val isButtonEnabled: Boolean,
        )
    }
}