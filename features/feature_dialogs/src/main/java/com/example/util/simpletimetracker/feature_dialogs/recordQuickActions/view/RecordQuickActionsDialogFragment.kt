package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.RecordQuickActionDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsWidthHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.createRecordQuickActionsButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.createRecordQuickActionsButtonBigAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.viewModel.RecordQuickActionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setSpanSizeLookup
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordQuickActionsDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordQuickActionsDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: RecordQuickActionsViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )

    private val contentAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordQuickActionsButtonBigAdapterDelegate(::onButtonClick),
            createRecordQuickActionsButtonAdapterDelegate(::onButtonClick),
        )
    }

    private val params: RecordQuickActionsParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordQuickActionsParams(),
    )
    private var listener: RecordQuickActionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener<RecordQuickActionDialogListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi(): Unit = with(binding) {
        rvRecordQuickActions.apply {
            val manager = GridLayoutManager(context, SPAN_COUNT)
            layoutManager = manager
            adapter = contentAdapter
            setIconsSpanSize(manager, contentAdapter)
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        state.observe(::updateState)
        actionComplete.observe { onActionComplete() }
        prepareRemoveRecordViewModel()
    }

    override fun onDataSelected(dataIds: List<Long>, tag: String?) {
        viewModel.onTypesSelected(dataIds, tag)
    }

    private fun updateState(state: RecordQuickActionsState) {
        contentAdapter.replace(state.buttons)
    }

    private fun prepareRemoveRecordViewModel() {
        val recordId = (params.type as? RecordQuickActionsParams.Type.RecordTracked)?.id.orZero()
        removeRecordViewModel.prepare(recordId)
    }

    private fun onActionComplete() {
        listener?.onActionComplete()
    }

    private fun setIconsSpanSize(
        layoutManager: GridLayoutManager?,
        adapter: BaseRecyclerAdapter,
    ) {
        layoutManager?.setSpanSizeLookup { position ->
            val item = adapter.getItemByPosition(position)
            val isFullWidth = item is RecordQuickActionsWidthHolder &&
                item.width is RecordQuickActionsWidthHolder.Width.Full
            if (isFullWidth) SPAN_COUNT else 1
        }
    }

    private fun onButtonClick(block: RecordQuickActionsButton) {
        viewModel.onButtonClick(block)
        if (block == RecordQuickActionsButton.DELETE) {
            removeRecordViewModel.onDeleteClick(ChangeRecordParams.From.Records)
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"
        private const val SPAN_COUNT = 2

        fun createBundle(data: RecordQuickActionsParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}