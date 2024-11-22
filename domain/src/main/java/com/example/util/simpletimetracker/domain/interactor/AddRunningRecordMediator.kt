package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.ResultContainer
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.domain.provider.CurrentTimestampProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddRunningRecordMediator @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRecordMediator: AddRecordMediator,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val notificationGoalCountInteractor: NotificationGoalCountInteractor,
    private val activityStartedStoppedBroadcastInteractor: ActivityStartedStoppedBroadcastInteractor,
    private val shouldShowRecordDataSelectionInteractor: ShouldShowRecordDataSelectionInteractor,
    private val pomodoroStartInteractor: PomodoroStartInteractor,
    private val complexRuleProcessActionInteractor: ComplexRuleProcessActionInteractor,
    private val updateExternalViewsInteractor: UpdateExternalViewsInteractor,
    private val currentTimestampProvider: CurrentTimestampProvider,
) {

    /**
     * Returns true if activity was started.
     */
    suspend fun tryStartTimer(
        typeId: Long,
        updateNotificationSwitch: Boolean = true,
        commentInputAvailable: Boolean = true,
        onNeedToShowTagSelection: suspend (RecordDataSelectionDialogResult) -> Unit,
    ): Boolean {
        // Already running
        if (runningRecordInteractor.get(typeId) != null) return false

        val shouldShowTagSelectionResult = shouldShowRecordDataSelectionInteractor.execute(
            typeId = typeId,
            commentInputAvailable = commentInputAvailable,
        )
        return if (shouldShowTagSelectionResult.fields.isNotEmpty()) {
            onNeedToShowTagSelection(shouldShowTagSelectionResult)
            false
        } else {
            startTimer(
                typeId = typeId,
                tagIds = emptyList(),
                comment = "",
                updateNotificationSwitch = updateNotificationSwitch,
            )
            true
        }
    }

    suspend fun startTimer(
        typeId: Long,
        tagIds: List<Long>,
        comment: String,
        timeStarted: StartTime = StartTime.TakeCurrent,
        updateNotificationSwitch: Boolean = true,
        checkDefaultDuration: Boolean = true,
    ) {
        val currentTime = currentTimestampProvider.get()
        val actualTimeStarted = when (timeStarted) {
            is StartTime.Current -> timeStarted.currentTimeStampMs
            is StartTime.TakeCurrent -> currentTime
            is StartTime.Timestamp -> timeStarted.timestampMs
        }
        val retroactiveTrackingMode = prefsInteractor.getRetroactiveTrackingMode()
        val actualPrevRecords = if (
            retroactiveTrackingMode ||
            complexRuleProcessActionInteractor.hasRules()
        ) {
            recordInteractor.getAllPrev(actualTimeStarted)
        } else {
            emptyList()
        }
        val rulesResult = if (
            retroactiveTrackingMode &&
            getPrevRecordToMergeWith(typeId, actualPrevRecords) != null
        ) {
            // No need to check rules on merge.
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                tagsIds = emptySet(),
            )
        } else {
            processRules(
                typeId = typeId,
                timeStarted = actualTimeStarted,
                prevRecords = actualPrevRecords,
            )
        }
        val isMultitaskingAllowedByDefault = prefsInteractor.getAllowMultitasking()
        val isMultitaskingAllowed = rulesResult.isMultitaskingAllowed.getValueOrNull()
            ?: isMultitaskingAllowedByDefault
        processMultitasking(
            typeId = typeId,
            isMultitaskingAllowed = isMultitaskingAllowed,
            splitTime = when (timeStarted) {
                is StartTime.Current -> timeStarted.currentTimeStampMs
                is StartTime.TakeCurrent -> currentTime
                is StartTime.Timestamp -> currentTime
            },
        )
        val actualTags = getAllTags(
            typeId = typeId,
            tagIds = tagIds,
            tagIdsFromRules = rulesResult.tagsIds,
        )
        activityStartedStoppedBroadcastInteractor.onActionActivityStarted(
            typeId = typeId,
            tagIds = actualTags,
            comment = comment,
        )
        val startParams = StartParams(
            typeId = typeId,
            comment = comment,
            tagIds = actualTags,
            timeStarted = actualTimeStarted,
            updateNotificationSwitch = updateNotificationSwitch,
            isMultitaskingAllowed = isMultitaskingAllowed,
        )
        if (retroactiveTrackingMode) {
            addRetroactiveModeInternal(startParams, actualPrevRecords)
        } else {
            addInternal(startParams, checkDefaultDuration)
        }
        // Show goal count only on timer start, otherwise it would show on change also.
        notificationGoalCountInteractor.checkAndShow(typeId)
        pomodoroStartInteractor.checkAndStart(typeId)
    }

    // Used separately only for changing running activity,
    // due to some poor (probably) decisions id of running record is it's type id,
    // so if type is changed - need to remove old and add new data.
    suspend fun addAfterChange(
        typeId: Long,
        timeStarted: Long,
        comment: String,
        tagIds: List<Long>,
    ) {
        addInternal(
            params = StartParams(
                typeId = typeId,
                timeStarted = timeStarted,
                comment = comment,
                tagIds = tagIds,
                updateNotificationSwitch = true,
                isMultitaskingAllowed = true,
            ),
            checkDefaultDuration = false,
        )
    }

    private suspend fun addInternal(
        params: StartParams,
        checkDefaultDuration: Boolean,
    ) {
        val type = recordTypeInteractor.get(params.typeId) ?: return
        if (type.defaultDuration > 0L && checkDefaultDuration) {
            addInstantRecord(params, type)
        } else {
            addRunningRecord(params)
        }
    }

    private suspend fun addRetroactiveModeInternal(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        val type = recordTypeInteractor.get(params.typeId) ?: return

        processRetroactiveMultitasking(
            params = params,
            prevRecords = prevRecords,
        )

        if (type.defaultDuration > 0L) {
            val newTimeStarted = params.timeStarted - type.defaultDuration * 1000
            addInstantRecord(
                params = params.copy(timeStarted = newTimeStarted),
                type = type,
            )
        } else {
            addRecordRetroactively(
                params = params,
                prevRecords = prevRecords,
            )
        }
    }

    private suspend fun addRunningRecord(
        params: StartParams,
    ) {
        if (runningRecordInteractor.get(params.typeId) == null && params.typeId > 0L) {
            val data = RunningRecord(
                id = params.typeId,
                timeStarted = params.timeStarted,
                comment = params.comment,
                tagIds = params.tagIds,
            )

            runningRecordInteractor.add(data)
            updateExternalViewsInteractor.onRunningRecordAdd(
                typeId = params.typeId,
                updateNotificationSwitch = params.updateNotificationSwitch,
            )
        }
    }

    private suspend fun addInstantRecord(
        params: StartParams,
        type: RecordType,
    ) {
        Record(
            typeId = params.typeId,
            timeStarted = params.timeStarted,
            timeEnded = params.timeStarted + type.defaultDuration * 1000,
            comment = params.comment,
            tagIds = params.tagIds,
        ).let {
            addRecordMediator.add(
                record = it,
                updateNotificationSwitch = params.updateNotificationSwitch,
            )
        }
    }

    private suspend fun addRecordRetroactively(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        val prevRecord = getPrevRecordToMergeWith(params.typeId, prevRecords)
        val record = if (prevRecord != null) {
            Record(
                id = prevRecord.id, // Updates existing record.
                typeId = params.typeId,
                timeStarted = prevRecord.timeStarted,
                timeEnded = params.timeStarted,
                comment = params.comment.takeUnless { it.isEmpty() }
                    ?: prevRecord.comment,
                tagIds = params.tagIds.takeUnless { it.isEmpty() }
                    ?: prevRecord.tagIds,
            )
        } else {
            val newTimeStarted = prevRecords.firstOrNull()?.timeEnded
                ?: (params.timeStarted - TimeUnit.MINUTES.toMillis(5))
            Record(
                id = 0L, // Creates new record.
                typeId = params.typeId,
                timeStarted = newTimeStarted,
                timeEnded = params.timeStarted,
                comment = params.comment,
                tagIds = params.tagIds,
            )
        }
        addRecordMediator.add(
            record = record,
            updateNotificationSwitch = params.updateNotificationSwitch,
        )
    }

    private suspend fun processRules(
        typeId: Long,
        timeStarted: Long,
        prevRecords: List<Record>,
    ): ComplexRuleProcessActionInteractor.Result {
        // If no rules - no need to check them.
        return if (complexRuleProcessActionInteractor.hasRules()) {
            // TODO do not check current records for Continue action?
            val currentRecords = runningRecordInteractor.getAll()

            // If no current records - check closest previous.
            val records = currentRecords.ifEmpty { prevRecords }

            val currentTypeIds = records
                .map { it.typeIds }
                .flatten()
                .toSet()

            complexRuleProcessActionInteractor.processRules(
                timeStarted = timeStarted,
                startingTypeId = typeId,
                currentTypeIds = currentTypeIds,
            )
        } else {
            ComplexRuleProcessActionInteractor.Result(
                isMultitaskingAllowed = ResultContainer.Undefined,
                tagsIds = emptySet(),
            )
        }
    }

    private suspend fun processMultitasking(
        typeId: Long,
        isMultitaskingAllowed: Boolean,
        splitTime: Long,
    ) {
        // Stop running records if multitasking is disabled.
        if (!isMultitaskingAllowed) {
            // Widgets will update on adding.
            runningRecordInteractor.getAll()
                .filter { it.id != typeId }
                .forEach {
                    removeRunningRecordMediator.removeWithRecordAdd(
                        runningRecord = it,
                        updateWidgets = false,
                        updateNotificationSwitch = false,
                        timeEnded = splitTime,
                    )
                }
        }
    }

    private suspend fun processRetroactiveMultitasking(
        params: StartParams,
        prevRecords: List<Record>,
    ) {
        if (!params.isMultitaskingAllowed) return

        val recordTypesMap = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val mergedRecord = getPrevRecordToMergeWith(params.typeId, prevRecords)

        // Extend prev records to current time.
        prevRecords.filter {
            // Skip record that would be merge.
            it.id != mergedRecord?.id
        }.filter {
            // Skip records with default duration.
            val thisType = recordTypesMap[it.typeId]
            thisType != null && thisType.defaultDuration == 0L
        }.map { prevRecord ->
            recordInteractor.updateTimeEnded(
                recordId = prevRecord.id,
                timeEnded = params.timeStarted,
            )
            prevRecord.typeId
        }.let {
            updateExternalViewsInteractor.onRecordTimeEndedChange(
                typeIds = it,
            )
        }
    }

    private suspend fun getAllTags(
        typeId: Long,
        tagIds: List<Long>,
        tagIdsFromRules: Set<Long>,
    ): List<Long> {
        val defaultTags = recordTypeToDefaultTagInteractor.getTags(typeId)
        return (tagIds + defaultTags + tagIdsFromRules).toSet().toList()
    }

    private fun getPrevRecordToMergeWith(
        typeId: Long,
        prevRecords: List<Record>,
    ): Record? {
        return prevRecords.firstOrNull { it.typeId == typeId }
    }

    private data class StartParams(
        val typeId: Long,
        val timeStarted: Long,
        val comment: String,
        val tagIds: List<Long>,
        val updateNotificationSwitch: Boolean,
        val isMultitaskingAllowed: Boolean,
    )

    sealed interface StartTime {
        data class Current(val currentTimeStampMs: Long) : StartTime
        data class Timestamp(val timestampMs: Long) : StartTime
        object TakeCurrent : StartTime
    }
}