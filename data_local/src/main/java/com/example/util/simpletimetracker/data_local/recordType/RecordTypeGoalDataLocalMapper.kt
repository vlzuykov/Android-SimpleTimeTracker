package com.example.util.simpletimetracker.data_local.recordType

import com.example.util.simpletimetracker.data_local.daysOfWeek.DaysOfWeekDataLocalMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import javax.inject.Inject

class RecordTypeGoalDataLocalMapper @Inject constructor(
    private val daysOfWeekDataLocalMapper: DaysOfWeekDataLocalMapper,
) {

    fun map(dbo: RecordTypeGoalDBO): RecordTypeGoal {
        return RecordTypeGoal(
            id = dbo.id,
            idData = if (dbo.typeId != 0L) {
                RecordTypeGoal.IdData.Type(dbo.typeId)
            } else {
                RecordTypeGoal.IdData.Category(dbo.categoryId)
            },
            range = when (dbo.range) {
                0L -> RecordTypeGoal.Range.Session
                1L -> RecordTypeGoal.Range.Daily
                2L -> RecordTypeGoal.Range.Weekly
                3L -> RecordTypeGoal.Range.Monthly
                else -> RecordTypeGoal.Range.Session
            },
            type = when (dbo.type) {
                0L -> RecordTypeGoal.Type.Duration(dbo.value)
                1L -> RecordTypeGoal.Type.Count(dbo.value)
                else -> RecordTypeGoal.Type.Duration(dbo.value)
            },
            subtype = when (dbo.subType) {
                0L -> RecordTypeGoal.Subtype.Goal
                1L -> RecordTypeGoal.Subtype.Limit
                else -> RecordTypeGoal.Subtype.Goal
            },
            daysOfWeek = daysOfWeekDataLocalMapper.mapDaysOfWeek(dbo.daysOfWeek),
        )
    }

    fun map(domain: RecordTypeGoal): RecordTypeGoalDBO {
        return RecordTypeGoalDBO(
            id = domain.id,
            typeId = (domain.idData as? RecordTypeGoal.IdData.Type)?.value.orZero(),
            range = when (domain.range) {
                is RecordTypeGoal.Range.Session -> 0L
                is RecordTypeGoal.Range.Daily -> 1L
                is RecordTypeGoal.Range.Weekly -> 2L
                is RecordTypeGoal.Range.Monthly -> 3L
            },
            type = when (domain.type) {
                is RecordTypeGoal.Type.Duration -> 0L
                is RecordTypeGoal.Type.Count -> 1L
            },
            subType = when (domain.subtype) {
                is RecordTypeGoal.Subtype.Goal -> 0L
                is RecordTypeGoal.Subtype.Limit -> 1L
            },
            value = domain.type.value,
            categoryId = (domain.idData as? RecordTypeGoal.IdData.Category)?.value.orZero(),
            daysOfWeek = daysOfWeekDataLocalMapper.mapDaysOfWeek(domain.daysOfWeek),
        )
    }
}