package com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion

import android.view.ViewGroup
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerBindingViewHolder
import com.example.util.simpletimetracker.feature_base_adapter.RecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordTypeLayoutBinding as BaseBinding
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData as BaseViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData as ViewData

// Wrapper around RecordType delegate.
// Passes all calls to other delegate.
fun createRecordTypeSuggestionAdapterDelegate(
    onItemClick: ((BaseViewData) -> Unit)? = null,
    onItemLongClick: ((BaseViewData, Pair<Any, String>) -> Unit)? = null,
): RecyclerAdapterDelegate {
    val baseAdapter = createRecordTypeAdapterDelegate(
        onItemClick = onItemClick,
        onItemLongClick = onItemLongClick,
        withTransition = true,
        transitionNamePrefix = TransitionNames.RECORD_TYPE_SUGGESTION,
    )

    return object : RecyclerAdapterDelegate {

        override fun isForValidType(check: ViewHolderType): Boolean {
            return check is ViewData
        }

        override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerBindingViewHolder<BaseBinding> {
            val baseViewHolder = baseAdapter.onCreateViewHolder(parent)
                as? BaseRecyclerBindingViewHolder<*>
            // Just in case, so it wouldn't crash.
            // Worst case - it would show unbound layout.
            val fallbackBinding by lazy {
                BaseBinding.inflate(parent.layoutInflater, parent, false)
            }

            return BaseRecyclerBindingViewHolder(
                binding = baseViewHolder?.binding as? BaseBinding ?: fallbackBinding,
                onBind = { _, item, payload ->
                    item as ViewData
                    baseViewHolder?.bind(item.data, payload)
                },
            )
        }

        override fun getViewHolderTypeName(): String = ViewData::class.java.simpleName
    }
}