package com.example.util.simpletimetracker.domain.model

data class WearCurrentState(
    val currentActivities: List<WearCurrentActivity>,
    val lastRecord: WearLastRecord,
)