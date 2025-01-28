package com.example.util.simpletimetracker.feature_complex_rules.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.button.createButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.complexRule.createComplexRuleAdapterDelegate
import com.example.util.simpletimetracker.feature_complex_rules.viewModel.ComplexRulesViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_complex_rules.databinding.ComplexRulesFragmentBinding as Binding

@AndroidEntryPoint
class ComplexRulesFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.rvComplexRulesList }

    private val viewModel: ComplexRulesViewModel by viewModels()

    private val rulesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createHintAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createComplexRuleAdapterDelegate(
                onItemClick = throttle(viewModel::onRuleClick),
                onDisableClick = viewModel::onRuleDisableClick,
            ),
            createButtonAdapterDelegate(throttle(viewModel::onItemButtonClick)),
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvComplexRulesList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rulesAdapter
            setHasFixedSize(true)
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe(rulesAdapter::replace)
    }
}
