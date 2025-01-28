package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterAddViewData
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import javax.inject.Inject

class ActivityFilterViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun map(
        filter: ActivityFilter,
        isDarkTheme: Boolean,
    ): ActivityFilterViewData {
        return mapFiltered(
            filter = filter,
            isDarkTheme = isDarkTheme,
            selected = filter.selected,
        )
    }

    fun mapFiltered(
        filter: ActivityFilter,
        isDarkTheme: Boolean,
        selected: Boolean,
    ): ActivityFilterViewData {
        return ActivityFilterViewData(
            id = filter.id,
            name = filter.name,
            iconColor = if (selected) {
                colorMapper.toIconColor(isDarkTheme)
            } else {
                colorMapper.toFilteredIconColor(isDarkTheme)
            },
            color = if (selected) {
                colorMapper.mapToColorInt(filter.color, isDarkTheme)
            } else {
                colorMapper.toFilteredColor(isDarkTheme)
            },
            selected = selected,
        )
    }

    fun mapToActivityFilterAddItem(
        isDarkTheme: Boolean,
    ): ActivityFilterAddViewData {
        return ActivityFilterAddViewData(
            name = resourceRepo.getString(R.string.running_records_add_filter),
            color = colorMapper.toInactiveColor(isDarkTheme),
        )
    }
}