package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionDuplicateMediator
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import javax.inject.Inject

class ChangeRecordActionsDuplicateDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordActionDuplicateMediator: RecordActionDuplicateMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsDuplicateDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadDuplicateViewData()
        parent?.update()
    }

    suspend fun onDuplicateClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionDuplicateMediator.execute(
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            timeEnded = params.newTimeEnded,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        )
        parent?.onSaveClickDelegate()
    }

    private suspend fun loadDuplicateViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.isAvailable) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_duplicate_hint),
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.DUPLICATE,
            isEnabled = params.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onSaveClickDelegate()

        data class ViewDataParams(
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newTimeEnded: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}