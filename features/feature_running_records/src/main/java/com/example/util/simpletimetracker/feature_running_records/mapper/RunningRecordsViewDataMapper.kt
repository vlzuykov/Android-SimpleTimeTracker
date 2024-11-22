package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RunningRecordViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordWithHint.RecordWithHintViewData
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.model.RunningRecordsFilterType
import javax.inject.Inject

class RunningRecordsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val runningRecordViewDataMapper: RunningRecordViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
) {

    fun mapToTypesEmpty(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(
                R.string.running_records_types_empty,
                resourceRepo.getString(R.string.running_records_add_type),
                resourceRepo.getString(R.string.running_records_add_default),
            ),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
        )
    }

    fun mapToHasRunningRecords(): ViewHolderType {
        return HintViewData(
            text = R.string.running_records_has_timers.let(resourceRepo::getString),
            paddingTop = 0,
            paddingBottom = 0,
        )
    }

    // TODO add hint about how it works and limitations?
    fun mapToRetroActiveMode(
        typesMap: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        prevRecords: List<Record>,
        isDarkTheme: Boolean,
        useProportionalMinutes: Boolean,
        useMilitaryTime: Boolean,
        showSeconds: Boolean,
        allowMultitasking: Boolean,
        multitaskingSelectionEnabled: Boolean,
        multiSelectedActivityIds: Set<Long>,
    ): List<ViewHolderType> {
        val result = mutableListOf<ViewHolderType>()

        if (prevRecords.isEmpty()) {
            result += EmptyViewData(
                message = resourceRepo.getString(R.string.retroactive_tracking_mode_hint),
                hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
            )
        }

        if (prevRecords.isNotEmpty()) {
            result += runningRecordViewDataMapper.map(
                runningRecord = RunningRecord(
                    id = UNTRACKED_ITEM_ID,
                    timeStarted = prevRecords.firstOrNull()?.timeEnded.orZero(),
                    comment = "",
                ),
                dailyCurrent = null,
                recordType = RecordType(
                    id = 0L,
                    name = resourceRepo.getString(R.string.untracked_time_name),
                    icon = "",
                    color = AppColor(
                        0, colorMapper.toUntrackedColor(isDarkTheme).toString(),
                    ),
                    defaultDuration = 0,
                    note = "",
                ),
                recordTags = emptyList(),
                goals = emptyList(),
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
                useProportionalMinutes = useProportionalMinutes,
                nowIconVisible = false,
                goalsVisible = false,
                totalDurationVisible = false,
            )
            result += prevRecords.mapNotNull { record ->
                val prevRecordType = typesMap[record.typeId]
                    ?: return@mapNotNull null
                val data = recordViewDataMapper.map(
                    record = record,
                    recordType = prevRecordType,
                    recordTags = recordTags.filter { it.id in record.tagIds },
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                )
                RecordWithHintViewData(data)
            }
            result += HintViewData(
                text = resourceRepo.getString(R.string.retroactive_tracking_mode_hint),
                paddingTop = 0,
                paddingBottom = 0,
            )
            if (allowMultitasking) {
                // TODO test retroactive mode
                // TODO test several prev records at the same time, merge accordingly.
                // TODO test retroactive multitask
                //  enable, go to other screen
                //  enable, go to edit type
                //  disable by clicking
                //  disable by removing
                result += DividerViewData(3)
                result += FilterViewData(
                    id = 0,
                    type = RunningRecordsFilterType.EnableMultitaskingSelection,
                    name = resourceRepo.getString(R.string.multitask_time_name),
                    color = if (multitaskingSelectionEnabled) {
                        colorMapper.toActiveColor(isDarkTheme)
                    } else {
                        colorMapper.toInactiveColor(isDarkTheme)
                    },
                    selected = multitaskingSelectionEnabled,
                    removeBtnVisible = multitaskingSelectionEnabled,
                )
                if (multitaskingSelectionEnabled) {
                    result += FilterViewData(
                        id = 1,
                        type = RunningRecordsFilterType.FinishMultitaskingSelection,
                        name = resourceRepo.getString(R.string.records_filter_select),
                        color = if (multiSelectedActivityIds.isNotEmpty()) {
                            colorMapper.toActiveColor(isDarkTheme)
                        } else {
                            colorMapper.toInactiveColor(isDarkTheme)
                        },
                        selected = false,
                        removeBtnVisible = false,
                    )
                }
            }
        }

        return result
    }
}