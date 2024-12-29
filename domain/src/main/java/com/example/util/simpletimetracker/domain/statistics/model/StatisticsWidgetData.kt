package com.example.util.simpletimetracker.domain.model

import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength

data class StatisticsWidgetData(
    val chartFilterType: ChartFilterType,
    val rangeLength: RangeLength,
    val filteredTypes: Set<Long>,
    val filteredCategories: Set<Long>,
    val filteredTags: Set<Long>,
)