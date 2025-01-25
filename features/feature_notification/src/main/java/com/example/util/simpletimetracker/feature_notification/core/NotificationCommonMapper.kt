package com.example.util.simpletimetracker.feature_notification.core

import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class NotificationCommonMapper @Inject constructor() {

    fun getNotificationText(
        recordType: RecordType,
        recordTags: List<RecordTag>,
    ): CharSequence {
        val tag = recordTags.getFullName()

        return buildSpannedString {
            bold { append(recordType.name) }
            if (tag.isNotEmpty()) {
                append(" - ")
                append(tag)
            }
        }
    }
}