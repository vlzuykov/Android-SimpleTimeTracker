package com.example.util.simpletimetracker.data_local.recordTag

import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToDefaultTag
import javax.inject.Inject

class RecordTypeToDefaultTagDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordTypeToDefaultTagDBO): RecordTypeToDefaultTag {
        return RecordTypeToDefaultTag(
            recordTypeId = dbo.recordTypeId,
            tagId = dbo.tagId,
        )
    }

    fun map(typeId: Long, tagId: Long): RecordTypeToDefaultTagDBO {
        return RecordTypeToDefaultTagDBO(
            recordTypeId = typeId,
            tagId = tagId,
        )
    }

    fun map(domain: RecordTypeToDefaultTag): RecordTypeToDefaultTagDBO {
        return RecordTypeToDefaultTagDBO(
            recordTypeId = domain.recordTypeId,
            tagId = domain.tagId,
        )
    }
}