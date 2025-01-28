package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RecordsFilterSelectedRecordsViewData(
    val isLoading: Boolean,
    val selectedRecordsCount: String,
    val showListButtonIsVisible: Boolean,
    val recordsViewData: List<ViewHolderType>,
)