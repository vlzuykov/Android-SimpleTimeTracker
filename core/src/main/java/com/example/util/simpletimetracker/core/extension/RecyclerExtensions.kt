package com.example.util.simpletimetracker.core.extension

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import java.util.Collections

fun RecyclerView.onItemMoved(
    getIsSelectable: (RecyclerView.ViewHolder?) -> Boolean = { true },
    getSelectablePositions: ((RecyclerView.ViewHolder?) -> Pair<Int, Int>)? = null,
    onSelected: (RecyclerView.ViewHolder?) -> Unit = {},
    onClear: (RecyclerView.ViewHolder) -> Unit = {},
    onMoved: (items: List<ViewHolderType>, from: Int, to: Int) -> Unit = { _, _, _ -> },
) {
    val dragDirections = ItemTouchHelper.DOWN or ItemTouchHelper.UP or
        ItemTouchHelper.START or ItemTouchHelper.END

    fun getNewItems(
        adapter: BaseRecyclerAdapter,
        fromPosition: Int,
        toPosition: Int,
    ): List<ViewHolderType> {
        val newList = adapter.currentList.toList()

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(newList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(newList, i, i - 1)
            }
        }

        return newList
    }

    val helper = object : ItemTouchHelper.SimpleCallback(0, 0) {

        override fun getDragDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
        ): Int {
            return if (getIsSelectable(viewHolder)) {
                dragDirections
            } else {
                0
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            getSelectablePositions?.invoke(viewHolder)?.let { (start, end) ->
                if (toPosition < start) return false
                if (toPosition > end) return false
            }

            (adapter as? BaseRecyclerAdapter)?.let { adapter ->
                val newItems = getNewItems(adapter, fromPosition, toPosition)
                adapter.submitList(newItems)
                onMoved(newItems, fromPosition, toPosition)
            }

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Do nothing
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) onSelected(viewHolder)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onClear(viewHolder)
        }
    }

    (ItemTouchHelper(helper)).attachToRecyclerView(this)
}