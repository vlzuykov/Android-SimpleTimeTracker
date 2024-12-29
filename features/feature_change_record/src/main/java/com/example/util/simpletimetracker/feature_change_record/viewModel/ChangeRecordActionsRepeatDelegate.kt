package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionRepeatMediator
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import javax.inject.Inject

class ChangeRecordActionsRepeatDelegate @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordActionRepeatMediator: RecordActionRepeatMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsRepeatDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadRepeatViewData()
        parent?.update()
    }

    suspend fun onRepeatClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        // Exit.
        parent?.onSaveClickDelegate(
            doAfter = {
                recordActionRepeatMediator.execute(
                    typeId = params.newTypeId,
                    comment = params.newComment,
                    tagIds = params.newCategoryIds,
                )
            },
        )
    }

    private fun loadRepeatViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.isAvailable) return emptyList()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_repeat_hint),
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.REPEAT,
            isEnabled = params.isButtonEnabled,
        )
        return result
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onSaveClickDelegate(doAfter: suspend () -> Unit)

        data class ViewDataParams(
            val newTypeId: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}