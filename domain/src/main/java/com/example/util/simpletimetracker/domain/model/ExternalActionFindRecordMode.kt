package com.example.util.simpletimetracker.domain.model

enum class ExternalActionFindRecordMode(val dataValue: String) {
    CURRENT_OR_LAST("current_or_last"),
    CURRENT("current"),
    LAST("last"),
}