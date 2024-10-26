package com.example.util.simpletimetracker.domain.model

sealed interface WearLastRecord {
    object None : WearLastRecord

    data class Present(
        val activityId: Long,
        val startedAt: Long,
        val finishedAt: Long,
        val tags: List<WearTag>,
    ) : WearLastRecord
}