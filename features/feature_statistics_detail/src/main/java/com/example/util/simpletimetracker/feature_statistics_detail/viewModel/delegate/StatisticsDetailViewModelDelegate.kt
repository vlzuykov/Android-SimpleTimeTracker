package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams

interface StatisticsDetailViewModelDelegate {

    fun attach(parent: Parent)

    interface Parent {
        val extra: StatisticsDetailParams
        val records: List<RecordBase>
        val compareRecords: List<RecordBase>
        val filter: List<RecordsFilter>
        val comparisonFilter: List<RecordsFilter>
        val rangeLength: RangeLength
        val rangePosition: Int

        fun updateContent()
        fun onRangeChanged()
        fun updateViewData()
        fun getDateFilter(): List<RecordsFilter>
        suspend fun onTypesFilterDismissed()
    }
}