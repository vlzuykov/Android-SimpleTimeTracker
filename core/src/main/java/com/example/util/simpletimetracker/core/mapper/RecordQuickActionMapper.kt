package com.example.util.simpletimetracker.core.mapper

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordQuickAction
import javax.inject.Inject

class RecordQuickActionMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapText(data: RecordQuickAction): String {
        return when (data) {
            RecordQuickAction.CONTINUE -> R.string.change_record_continue
            RecordQuickAction.REPEAT -> R.string.change_record_repeat
            RecordQuickAction.DUPLICATE -> R.string.change_record_duplicate
            RecordQuickAction.MERGE -> R.string.change_record_merge
            RecordQuickAction.SPLIT -> R.string.change_record_split
            RecordQuickAction.ADJUST -> R.string.change_record_adjust
            RecordQuickAction.STOP -> R.string.notification_record_type_stop
            RecordQuickAction.CHANGE_ACTIVITY -> R.string.data_edit_change_activity
        }.let(resourceRepo::getString)
    }

    @DrawableRes
    fun mapIcon(data: RecordQuickAction): Int {
        return when (data) {
            RecordQuickAction.CONTINUE -> R.drawable.action_continue
            RecordQuickAction.REPEAT -> R.drawable.repeat
            RecordQuickAction.DUPLICATE -> R.drawable.action_copy
            RecordQuickAction.MERGE -> R.drawable.action_merge
            RecordQuickAction.SPLIT -> R.drawable.action_divide
            RecordQuickAction.ADJUST -> R.drawable.action_change
            RecordQuickAction.STOP -> R.drawable.action_stop
            RecordQuickAction.CHANGE_ACTIVITY -> R.drawable.action_change_item
        }
    }

    @ColorInt
    fun mapColor(data: RecordQuickAction): Int {
        return when (data) {
            RecordQuickAction.CONTINUE -> R.color.red_300
            RecordQuickAction.REPEAT -> R.color.purple_300
            RecordQuickAction.DUPLICATE -> R.color.indigo_300
            RecordQuickAction.MERGE -> R.color.light_blue_300
            RecordQuickAction.STOP -> R.color.teal_300
            RecordQuickAction.CHANGE_ACTIVITY -> R.color.green_300
            RecordQuickAction.SPLIT -> R.color.pink_300
            RecordQuickAction.ADJUST -> R.color.amber_300
        }.let(resourceRepo::getColor)
    }
}