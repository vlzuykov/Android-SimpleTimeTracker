package com.example.util.simpletimetracker.domain.record.model

data class RecordDataSelectionDialogResult(
    val fields: List<Field>,
) {
    sealed interface Field {
        object Tags : Field
        object Comment : Field
    }
}