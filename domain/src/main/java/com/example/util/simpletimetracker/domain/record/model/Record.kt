package com.example.util.simpletimetracker.domain.record.model

data class Record(
    val id: Long = 0,
    val typeId: Long,
    override val timeStarted: Long,
    override val timeEnded: Long,
    override val comment: String,
    override val tagIds: List<Long> = emptyList(),
) : RecordBase {

    override val typeIds: List<Long> = listOf(typeId)
}