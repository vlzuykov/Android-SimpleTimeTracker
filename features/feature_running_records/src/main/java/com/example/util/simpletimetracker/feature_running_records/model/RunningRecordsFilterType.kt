package com.example.util.simpletimetracker.feature_running_records.model

import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData

sealed interface RunningRecordsFilterType : FilterViewData.Type {
    object EnableMultitaskingSelection : RunningRecordsFilterType
    object FinishMultitaskingSelection : RunningRecordsFilterType
}