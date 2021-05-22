package com.example.util.simpletimetracker.core.adapter.category

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.createRecyclerAdapterDelegate
import com.example.util.simpletimetracker.core.extension.setOnClickWith
import kotlinx.android.synthetic.main.item_category_layout.view.viewCategoryItem

fun createCategoryAdapterDelegate(
    onItemClick: ((CategoryViewData) -> Unit)
) = createRecyclerAdapterDelegate<CategoryViewData>(
    R.layout.item_category_layout
) { itemView, item, _ ->

    with(itemView.viewCategoryItem) {
        item as CategoryViewData

        itemColor = item.color
        itemName = item.name
        itemTextColor = item.textColor
        (item as? CategoryViewData.Record)?.icon?.let {
            itemIconVisible = true
            itemIcon = it
        } ?: run {
            itemIconVisible = false
        }

        setOnClickWith(item, onItemClick)
    }
}