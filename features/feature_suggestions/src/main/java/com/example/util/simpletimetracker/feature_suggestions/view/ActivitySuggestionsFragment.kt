package com.example.util.simpletimetracker.feature_suggestions.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.adapter.createActivitySuggestionListAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.adapter.createActivitySuggestionSpecialAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.adapter.createActivitySuggestionsButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.viewModel.ActivitySuggestionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_suggestions.databinding.ActivitySuggestionsFragmentBinding as Binding

@AndroidEntryPoint
class ActivitySuggestionsFragment :
    BaseFragment<Binding>(),
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ActivitySuggestionsViewModel by viewModels()

    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDividerAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createActivitySuggestionListAdapterDelegate(),
            createActivitySuggestionSpecialAdapterDelegate(throttle(viewModel::onSpecialSuggestionClick)),
            createActivitySuggestionsButtonAdapterDelegate(throttle(viewModel::onItemButtonClick)),
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvActivitySuggestionsList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = viewDataAdapter
            setHasFixedSize(true)
        }
    }

    override fun initUx() = with(binding) {
        btnActivitySuggestionsSave.setOnClick(throttle(viewModel::onSaveClick))
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe(viewDataAdapter::replace)
    }

    override fun onDataSelected(dataIds: List<Long>, tag: String?) {
        viewModel.onTypesSelected(dataIds, tag)
    }
}
