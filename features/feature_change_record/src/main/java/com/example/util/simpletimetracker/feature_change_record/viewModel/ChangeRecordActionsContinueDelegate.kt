package com.example.util.simpletimetracker.feature_change_record.viewModel

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordAction.interactor.RecordActionContinueMediator
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.mapper.ChangeRecordViewDataMapper
import com.example.util.simpletimetracker.navigation.Router
import javax.inject.Inject

class ChangeRecordActionsContinueDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordActionContinueMediator: RecordActionContinueMediator,
    private val changeRecordViewDataMapper: ChangeRecordViewDataMapper,
) : ChangeRecordActionsSubDelegate<ChangeRecordActionsContinueDelegate.Parent> {

    private var parent: Parent? = null
    private var viewData: List<ViewHolderType> = emptyList()

    override fun attach(parent: Parent) {
        this.parent = parent
    }

    override fun getViewData(): List<ViewHolderType> {
        return viewData
    }

    override suspend fun updateViewData() {
        viewData = loadContinueViewData()
        parent?.update()
    }

    suspend fun onContinueClickDelegate() {
        val params = parent?.getViewDataParams() ?: return
        recordActionContinueMediator.execute(
            recordId = params.originalRecordId,
            typeId = params.newTypeId,
            timeStarted = params.newTimeStarted,
            comment = params.newComment,
            tagIds = params.newCategoryIds,
        )
        // Exit.
        router.back()
    }

    fun canContinue(): Boolean {
        val params = parent?.getViewDataParams() ?: return false

        // Can't continue future record
        return if (params.newTimeStarted > System.currentTimeMillis()) {
            parent?.showMessage(R.string.cannot_be_in_the_future)
            false
        } else {
            true
        }
    }

    private suspend fun loadContinueViewData(): List<ViewHolderType> {
        val params = parent?.getViewDataParams()
            ?: return emptyList()
        if (!params.isAvailable) return emptyList()
        if (prefsInteractor.getRetroactiveTrackingMode()) return emptyList()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val result = mutableListOf<ViewHolderType>()
        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_continue_hint),
        )
        result += changeRecordViewDataMapper.mapRecordActionButton(
            action = RecordQuickAction.CONTINUE,
            isEnabled = params.isButtonEnabled,
            isDarkTheme = isDarkTheme,
        )
        return result
    }

    interface Parent {

        fun getViewDataParams(): ViewDataParams?
        fun update()
        suspend fun onSaveClickDelegate()
        fun showMessage(stringResId: Int)

        data class ViewDataParams(
            val originalRecordId: Long,
            val newTypeId: Long,
            val newTimeStarted: Long,
            val newComment: String,
            val newCategoryIds: List<Long>,
            val isAvailable: Boolean,
            val isButtonEnabled: Boolean,
        )
    }
}