package com.example.util.simpletimetracker.feature_base_adapter.runningRecord

data class GoalTimeViewData(
    val text: String,
    val complete: Boolean,
    val state: Subtype,
) {

    sealed interface Subtype {
        data object Goal : Subtype
        data object Limit : Subtype
    }
}