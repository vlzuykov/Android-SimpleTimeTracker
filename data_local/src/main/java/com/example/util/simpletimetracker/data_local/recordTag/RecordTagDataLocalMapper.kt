package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import javax.inject.Inject

class RecordTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTagDBO): RecordTag {
        return RecordTag(
            id = dbo.id,
            name = dbo.name,
            icon = dbo.icon,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
            ),
            iconColorSource = dbo.iconColorSource,
            note = dbo.note,
            archived = dbo.archived,
        )
    }

    fun map(domain: RecordTag): RecordTagDBO {
        return RecordTagDBO(
            id = domain.id,
            typeId = 0,
            name = domain.name,
            icon = domain.icon,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            iconColorSource = domain.iconColorSource,
            note = domain.note,
            archived = domain.archived,
        )
    }
}