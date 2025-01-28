package com.example.util.simpletimetracker.data_local.recordType

import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class RecordTypeDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeDBO): RecordType {
        return RecordType(
            id = dbo.id,
            name = dbo.name,
            icon = dbo.icon,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
            ),
            defaultDuration = dbo.defaultDuration,
            note = dbo.note,
            hidden = dbo.hidden,
        )
    }

    fun map(domain: RecordType): RecordTypeDBO {
        return RecordTypeDBO(
            id = domain.id,
            name = domain.name,
            icon = domain.icon,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            defaultDuration = domain.defaultDuration,
            note = domain.note,
            hidden = domain.hidden,
        )
    }
}