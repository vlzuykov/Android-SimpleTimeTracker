package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordQuickAction
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.ADJUST
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.CHANGE_ACTIVITY
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.CHANGE_TAG
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.CONTINUE
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.DUPLICATE
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.MERGE
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.REPEAT
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.SPLIT
import com.example.util.simpletimetracker.domain.model.RecordQuickAction.STOP
import javax.inject.Inject

class RecordQuickActionMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapText(data: RecordQuickAction): String {
        return when (data) {
            CONTINUE -> R.string.change_record_continue
            REPEAT -> R.string.change_record_repeat
            DUPLICATE -> R.string.change_record_duplicate
            MERGE -> R.string.change_record_merge
            SPLIT -> R.string.change_record_split
            ADJUST -> R.string.change_record_adjust
            STOP -> R.string.notification_record_type_stop
            CHANGE_ACTIVITY -> R.string.data_edit_change_activity
            CHANGE_TAG -> R.string.data_edit_change_tag
        }.let(resourceRepo::getString)
    }

    @DrawableRes
    fun mapIcon(data: RecordQuickAction): Int {
        return when (data) {
            CONTINUE -> R.drawable.action_continue
            REPEAT -> R.drawable.repeat
            DUPLICATE -> R.drawable.action_copy
            MERGE -> R.drawable.action_merge
            SPLIT -> R.drawable.action_divide
            ADJUST -> R.drawable.action_change
            STOP -> R.drawable.action_stop
            CHANGE_ACTIVITY, CHANGE_TAG -> R.drawable.action_change_item
        }
    }

    @ColorInt
    fun mapColor(data: RecordQuickAction): Int {
        return when (data) {
            CONTINUE -> R.color.red_300
            REPEAT -> R.color.purple_300
            DUPLICATE -> R.color.indigo_300
            MERGE -> R.color.light_blue_300
            STOP -> R.color.teal_300
            CHANGE_ACTIVITY, CHANGE_TAG -> R.color.green_300
            SPLIT -> R.color.pink_300
            ADJUST -> R.color.amber_300
        }.let(resourceRepo::getColor)
    }
}