package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import javax.inject.Inject

class RecordTypeToTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeToTagDBO): RecordTypeToTag {
        return RecordTypeToTag(
            recordTypeId = dbo.recordTypeId,
            tagId = dbo.tagId,
        )
    }

    fun map(typeId: Long, tagId: Long): RecordTypeToTagDBO {
        return RecordTypeToTagDBO(
            recordTypeId = typeId,
            tagId = tagId,
        )
    }

    fun map(domain: RecordTypeToTag): RecordTypeToTagDBO {
        return RecordTypeToTagDBO(
            recordTypeId = domain.recordTypeId,
            tagId = domain.tagId,
        )
    }
}