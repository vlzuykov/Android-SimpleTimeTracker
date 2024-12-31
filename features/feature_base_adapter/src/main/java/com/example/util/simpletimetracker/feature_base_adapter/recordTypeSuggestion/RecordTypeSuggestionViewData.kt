package com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData

data class RecordTypeSuggestionViewData(
    val data: RecordTypeViewData,
) : ViewHolderType {

    override fun getUniqueId(): Long = data.id

    override fun isValidType(other: ViewHolderType): Boolean = other is RecordTypeSuggestionViewData
}