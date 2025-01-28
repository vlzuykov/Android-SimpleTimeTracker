package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordChangePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordSliderViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimeAdjustmentViewData
import com.example.util.simpletimetracker.feature_change_record.adapter.ChangeRecordTimePreviewViewData
import com.example.util.simpletimetracker.feature_change_record.interactor.ChangeRecordViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordPreview
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.ensureActive

class ChangeRecordActionsSplitDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val changeRecordViewDataInteractor: ChangeRecordViewDataInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsSplitDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        coroutineScope {
            viewData = loadViewData()
            ensureActive()
            parent?.update()
        }
    }

    suspend fun onSplitClickDelegate() {
        val params = parent?.getViewDataParams() ?: return

        Record(
            id = 0L, // Zero id creates new record
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            timeEnded = params.newTimeSplit,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        ).let {
            addRecordMediator.add(it)
        }
        parent?.onSplitComplete()
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        val newTimeSplit = params.newTimeSplit
        val newTypeId = params.newTypeId
        val newTimeStarted = params.newTimeStarted
        val newTimeEnded = params.splitPreviewTimeEnded
        val showTimeEnded = params.showTimeEndedOnSplitPreview
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(resourceRepo.getString(R.string.change_record_split_hint))
        result += ChangeRecordTimePreviewViewData(
            block = ChangeRecordActionsBlock.SplitTimePreview,
            text = loadTimeSplitValue(newTimeSplit),
        )
        result += ChangeRecordTimeAdjustmentViewData(
            block = ChangeRecordActionsBlock.SplitTimeAdjustment,
            items = loadTimeSplitAdjustmentItems(),
        )
        result += ChangeRecordSliderViewData(
            block = ChangeRecordActionsBlock.SplitSlider,
            min = 0f,
            max = TimeUnit.MILLISECONDS.toSeconds(newTimeEnded - newTimeStarted).toFloat(),
            value = TimeUnit.MILLISECONDS.toSeconds(newTimeSplit - newTimeStarted).toFloat(),
        )
        val previewData = loadSplitPreviewViewData(
            newTypeId = newTypeId,
            newTimeStarted = newTimeStarted,
            newTimeSplit = newTimeSplit,
            newTimeEnded = newTimeEnded,
            showTimeEnded = showTimeEnded,
        )
        result += ChangeRecordChangePreviewViewData(
            id = previewData.id,
            before = previewData.before,
            after = previewData.after,
            marginTopDp = 2,
            isChecked = false,
            isRemoveVisible = false,
            isCheckVisible = false,
            isCompareVisible = false,
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.SPLIT,
            isEnabled = params.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    private fun loadTimeSplitAdjustmentItems(): List<ViewHolderType> {
        return changeRecordViewDataInteractor.getTimeAdjustmentItems(
            dateTimeFieldState = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
    }

    private suspend fun loadTimeSplitValue(
        newTimeSplit: Long,
    ): String {
        return changeRecordViewDataInteractor.mapTime(newTimeSplit)
    }

    private suspend fun loadSplitPreviewViewData(
        newTypeId: Long,
        newTimeStarted: Long,
        newTimeSplit: Long,
        newTimeEnded: Long,
        showTimeEnded: Boolean,
    ): ChangeRecordPreview {
        val dateTimeFieldState = ChangeRecordDateTimeFieldsState(
            start = ChangeRecordDateTimeFieldsState.State.DateTime,
            end = ChangeRecordDateTimeFieldsState.State.DateTime,
        )
        val firstRecord = Record(
            typeId = newTypeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeSplit,
            comment = "",
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it, dateTimeFieldState)
        }
        val secondRecord = Record(
            typeId = newTypeId,
            timeStarted = newTimeSplit,
            timeEnded = newTimeEnded,
            comment = "",
        ).let {
            changeRecordViewDataInteractor.getPreviewViewData(it, dateTimeFieldState)
        }

        return ChangeRecordPreview(
            id = 0,
            before = changeRecordViewDataMapper.mapSimple(
                preview = firstRecord,
                showTimeEnded = true,
                timeStartedChanged = false,
                timeEndedChanged = true,
            ),
            after = changeRecordViewDataMapper.mapSimple(
                preview = secondRecord,
                showTimeEnded = showTimeEnded,
                timeStartedChanged = true,
                timeEndedChanged = false,
            ),
        )
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onSplitComplete()

        data class ViewDataParams(
            val newTimeSplit: Long,
            val newTypeId: Long,
            val newTimeStarted: Long,
            val splitPreviewTimeEnded: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val showTimeEndedOnSplitPreview: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}