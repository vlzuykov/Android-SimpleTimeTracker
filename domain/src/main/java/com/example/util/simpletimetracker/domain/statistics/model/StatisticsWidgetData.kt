package com.example.util.simpletimetracker.domain.statistics.model

data class StatisticsWidgetData(
    val chartFilterType: ChartFilterType,
    val rangeLength: RangeLength,
    val filteredTypes: Set<Long>,
    val filteredCategories: Set<Long>,
    val filteredTags: Set<Long>,
)