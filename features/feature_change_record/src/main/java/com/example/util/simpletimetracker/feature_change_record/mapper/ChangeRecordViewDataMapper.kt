package com.example.util.simpletimetracker.feature_change_record.mapper

import com.example.util.simpletimetracker.core.mapper.ChangeRecordDateTimeMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.getFullName
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordAction.model.RecordQuickAction
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordActionsBlock
import com.example.util.simpletimetracker.feature_change_record.model.ChangeRecordDateTimeFieldsState
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordSimpleViewData
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import javax.inject.Inject

class ChangeRecordViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val changeRecordDateTimeMapper: ChangeRecordDateTimeMapper,
    private val recordQuickActionMapper: RecordQuickActionMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showSeconds: Boolean,
        dateTimeFieldState: ChangeRecordDateTimeFieldsState,
    ): ChangeRecordViewData {
        return ChangeRecordViewData(
            name = recordType?.name
                ?: resourceRepo.getString(R.string.untracked_time_name),
            tagName = recordTags
                .getFullName(),
            timeStarted = timeMapper.formatTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeFinished = timeMapper.formatTime(
                time = record.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            dateTimeStarted = changeRecordDateTimeMapper.map(
                param = when (dateTimeFieldState.start) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        ChangeRecordDateTimeMapper.Param.DateTime(record.timeStarted)
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        ChangeRecordDateTimeMapper.Param.Duration(record.duration)
                    }
                },
                field = ChangeRecordDateTimeMapper.Field.Start,
                useMilitaryTimeFormat = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            dateTimeFinished = changeRecordDateTimeMapper.map(
                param = when (dateTimeFieldState.end) {
                    is ChangeRecordDateTimeFieldsState.State.DateTime -> {
                        ChangeRecordDateTimeMapper.Param.DateTime(record.timeEnded)
                    }
                    is ChangeRecordDateTimeFieldsState.State.Duration -> {
                        ChangeRecordDateTimeMapper.Param.Duration(record.duration)
                    }
                },
                field = ChangeRecordDateTimeMapper.Field.End,
                useMilitaryTimeFormat = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            duration = timeMapper.formatInterval(
                interval = recordViewDataMapper.mapDuration(
                    record = record,
                    showSeconds = showSeconds,
                ),
                forceSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
            ),
            iconId = recordType?.icon.orEmpty()
                .let(iconMapper::mapIcon),
            color = recordType?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: colorMapper.toUntrackedColor(isDarkTheme),
            comment = record.comment,
        )
    }

    fun mapSimple(
        preview: ChangeRecordViewData,
        showTimeEnded: Boolean,
        timeStartedChanged: Boolean,
        timeEndedChanged: Boolean,
    ): ChangeRecordSimpleViewData {
        return ChangeRecordSimpleViewData(
            name = preview.name,
            timeStarted = preview.timeStarted,
            timeEnded = if (showTimeEnded) {
                preview.timeFinished
            } else {
                ""
            },
            timeStartedChanged = timeStartedChanged,
            timeEndedChanged = timeEndedChanged,
            duration = preview.duration,
            iconId = preview.iconId,
            color = preview.color,
        )
    }

    fun mapRecordActionButton(
        action: RecordQuickAction,
        isEnabled: Boolean,
        isDarkTheme: Boolean,
    ): ButtonViewData? {
        return ButtonViewData(
            id = ChangeRecordQuickActionsButtonViewData(
                block = mapRecordAction(action) ?: return null,
            ),
            text = recordQuickActionMapper.mapText(action),
            icon = ButtonViewData.Icon.Present(
                icon = recordQuickActionMapper.mapIcon(action),
                iconColor = resourceRepo.getThemedAttr(R.attr.appCardBackgroundColor, isDarkTheme),
                iconBackgroundColor = recordQuickActionMapper.mapColor(action),
            ),
            backgroundColor = resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme),
            isEnabled = isEnabled,
            marginHorizontalDp = 4,
        )
    }

    private fun mapRecordAction(
        action: RecordQuickAction,
    ): ChangeRecordActionsBlock? {
        return when (action) {
            RecordQuickAction.CONTINUE -> ChangeRecordActionsBlock.ContinueButton
            RecordQuickAction.REPEAT -> ChangeRecordActionsBlock.RepeatButton
            RecordQuickAction.DUPLICATE -> ChangeRecordActionsBlock.DuplicateButton
            RecordQuickAction.MERGE -> ChangeRecordActionsBlock.MergeButton
            RecordQuickAction.SPLIT -> ChangeRecordActionsBlock.SplitButton
            RecordQuickAction.ADJUST -> ChangeRecordActionsBlock.AdjustButton
            RecordQuickAction.STOP -> null
            RecordQuickAction.CHANGE_ACTIVITY -> null
            RecordQuickAction.CHANGE_TAG -> null
        }
    }
}