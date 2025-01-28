package com.example.util.simpletimetracker.domain.record.model

// TODO switch to typealias to avoid object creation.
data class Range(
    val timeStarted: Long,
    val timeEnded: Long,
) {

    val duration: Long = timeEnded - timeStarted

    fun isOverlappingWith(other: Range): Boolean {
        return timeStarted < other.timeEnded && timeEnded > other.timeStarted
    }
}