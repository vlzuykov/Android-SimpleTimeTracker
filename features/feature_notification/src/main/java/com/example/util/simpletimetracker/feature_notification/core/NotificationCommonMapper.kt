package com.example.util.simpletimetracker.feature_notification.core

import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class NotificationCommonMapper @Inject constructor() {

    fun getNotificationText(
        recordType: RecordType,
        recordTags: List<RecordTag>,
    ): String {
        val tag = recordTags.getFullName()

        return if (tag.isEmpty()) {
            recordType.name
        } else {
            "${recordType.name} - $tag"
        }
    }
}