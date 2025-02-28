package com.example.util.simpletimetracker.feature_base_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater

interface RecyclerAdapterDelegate {
    fun isForValidType(check: ViewHolderType): Boolean

    fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerViewHolder

    fun getViewHolderTypeName(): String
}

abstract class BaseRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    abstract fun bind(item: ViewHolderType, payloads: List<Any>)
}

inline fun <reified T : ViewHolderType, B : ViewBinding> createRecyclerBindingAdapterDelegate(
    noinline inflater: (LayoutInflater, ViewGroup?, Boolean) -> B,
    noinline onBind: (B, ViewHolderType, List<Any>) -> Unit,
) = object : RecyclerAdapterDelegate {

    override fun isForValidType(check: ViewHolderType): Boolean {
        return check is T
    }

    override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerBindingViewHolder<B> =
        BaseRecyclerBindingViewHolder(
            binding = inflater(parent.layoutInflater, parent, false),
            onBind = onBind,
        )

    override fun getViewHolderTypeName(): String = T::class.java.simpleName
}

class BaseRecyclerBindingViewHolder<B : ViewBinding>(
    val binding: B,
    private val onBind: (B, ViewHolderType, List<Any>) -> Unit,
) : BaseRecyclerViewHolder(binding.root) {

    override fun bind(item: ViewHolderType, payloads: List<Any>) =
        onBind(binding, item, payloads)
}