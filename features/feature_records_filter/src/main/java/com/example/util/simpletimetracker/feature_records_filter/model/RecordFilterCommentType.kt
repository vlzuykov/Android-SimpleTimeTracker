package com.example.util.simpletimetracker.feature_records_filter.model

import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

sealed interface RecordFilterCommentType : FilterViewData.Type {
    object NoComment : RecordFilterCommentType
    object AnyComment : RecordFilterCommentType
}