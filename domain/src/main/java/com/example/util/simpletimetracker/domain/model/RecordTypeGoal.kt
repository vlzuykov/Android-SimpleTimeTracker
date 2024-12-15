package com.example.util.simpletimetracker.domain.model

data class RecordTypeGoal(
    val id: Long = 0,
    val idData: IdData,
    val range: Range,
    val type: Type,
    val subType: Subtype,
    val daysOfWeek: List<DayOfWeek>,
) {

    sealed interface IdData {
        val value: Long

        data class Type(override val value: Long) : IdData
        data class Category(override val value: Long) : IdData
    }

    sealed interface Range {
        data object Session : Range
        data object Daily : Range
        data object Weekly : Range
        data object Monthly : Range
    }

    sealed interface Type {
        val value: Long

        data class Duration(override val value: Long) : Type
        data class Count(override val value: Long) : Type
    }

    sealed interface Subtype {
        data object Goal: Subtype
        data object Limit: Subtype
    }
}