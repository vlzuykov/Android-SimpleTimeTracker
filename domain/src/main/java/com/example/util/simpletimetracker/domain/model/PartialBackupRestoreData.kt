package com.example.util.simpletimetracker.domain.model

// TODO switch to LongObjectMap from androidx.collections
data class PartialBackupRestoreData(
    val types: Map<Long, Holder<RecordType>>,
    val records: Map<Long, Holder<Record>>,
    val categories: Map<Long, Holder<Category>>,
    val typeToCategory: List<Holder<RecordTypeCategory>>,
    val tags: Map<Long, Holder<RecordTag>>,
    val recordToTag: List<Holder<RecordToRecordTag>>,
    val typeToTag: List<Holder<RecordTypeToTag>>,
    val typeToDefaultTag: List<Holder<RecordTypeToDefaultTag>>,
    val activityFilters: Map<Long, Holder<ActivityFilter>>,
    val favouriteComments: Map<Long, Holder<FavouriteComment>>,
    val favouriteColors: Map<Long, Holder<FavouriteColor>>,
    val favouriteIcon: Map<Long, Holder<FavouriteIcon>>,
    val goals: Map<Long, Holder<RecordTypeGoal>>,
    val rules: Map<Long, Holder<ComplexRule>>,
) {

    data class Holder<T>(
        val exist: Boolean,
        val data: T,
    )
}

fun <T> Collection<PartialBackupRestoreData.Holder<T>>.getNotExistingValues(): List<T> {
    return this.mapNotNull { if (!it.exist) it.data else null }
}

fun <T> Collection<PartialBackupRestoreData.Holder<T>>.getExistingValues(): List<T> {
    return this.mapNotNull { if (it.exist) it.data else null }
}