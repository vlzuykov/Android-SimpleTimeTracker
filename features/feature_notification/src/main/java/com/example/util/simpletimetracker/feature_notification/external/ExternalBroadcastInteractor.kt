package com.example.util.simpletimetracker.feature_notification.external

import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.record.interactor.AddRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.notifications.model.ExternalActionCommentMode
import com.example.util.simpletimetracker.domain.notifications.model.ExternalActionFindRecordMode
import com.example.util.simpletimetracker.domain.record.model.Record
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ExternalBroadcastInteractor @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val addRecordMediator: AddRecordMediator,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
) {

    suspend fun onActionActivityStart(
        name: String,
        comment: String?,
        tagNames: List<String>,
        timeStarted: String?,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val runningRecord = runningRecordInteractor.get(typeId)
        if (runningRecord != null) return // Already running.
        val tagIds = findTagIdByName(tagNames, typeId)
        val newTimeStarted = timeStarted?.let(::parseTimestamp)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment.orEmpty(),
            tagIds = tagIds,
            timeStarted = if (newTimeStarted != null) {
                AddRunningRecordMediator.StartTime.Timestamp(newTimeStarted)
            } else {
                AddRunningRecordMediator.StartTime.TakeCurrent
            },
        )
    }

    suspend fun onActionActivityStopByName(
        name: String,
        timeEnded: String?,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val newTimeEnded = timeEnded?.let(::parseTimestamp)
        val runningRecord = runningRecordInteractor.get(typeId)
            ?: return // Not running.

        removeRunningRecordMediator.removeWithRecordAdd(
            runningRecord = runningRecord,
            timeEnded = newTimeEnded,
        )
    }

    suspend fun onActionActivityStopAll() {
        runningRecordInteractor.getAll()
            .forEach { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityStopShortest() {
        runningRecordInteractor.getAll()
            .maxByOrNull { it.timeStarted }
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityStopLongest() {
        runningRecordInteractor.getAll()
            .minByOrNull { it.timeStarted }
            ?.let { removeRunningRecordMediator.removeWithRecordAdd(it) }
    }

    suspend fun onActionActivityRestart(
        comment: String?,
        tagNames: List<String>,
    ) {
        val previousRecord = recordInteractor.getPrev(
            timeStarted = System.currentTimeMillis(),
        ) ?: return
        val typeId = previousRecord.typeId
        val tagIds = findTagIdByName(tagNames, typeId)

        addRunningRecordMediator.startTimer(
            typeId = typeId,
            comment = comment
                ?: previousRecord.comment,
            tagIds = tagIds
                .takeUnless { tagNames.isEmpty() }
                ?: previousRecord.tagIds,
        )
    }

    suspend fun onRecordAdd(
        name: String,
        timeStarted: String,
        timeEnded: String,
        comment: String?,
        tagNames: List<String>,
    ) {
        val typeId = getTypeIdByName(name) ?: return
        val newTimeStarted = parseTimestamp(timeStarted) ?: return
        val newTimeEnded = parseTimestamp(timeEnded) ?: return
        val tagIds = findTagIdByName(tagNames, typeId)

        Record(
            id = 0, // Zero creates new record.
            typeId = typeId,
            timeStarted = newTimeStarted,
            timeEnded = newTimeEnded,
            comment = comment.orEmpty(),
            tagIds = tagIds,
        ).let {
            addRecordMediator.add(it)
            recordsUpdateInteractor.send()
        }
    }

    suspend fun onRecordChange(
        findModeData: String?,
        name: String?,
        comment: String?,
        commentModeData: String?,
    ) {
        val typeId = name?.let { getTypeIdByName(it) }
        val findMode = ExternalActionFindRecordMode.entries.firstOrNull {
            it.dataValue == findModeData
        } ?: ExternalActionFindRecordMode.CURRENT_OR_LAST
        val commentMode = ExternalActionCommentMode.entries.firstOrNull {
            it.dataValue == commentModeData
        } ?: ExternalActionCommentMode.SET

        fun processComment(
            oldComment: String,
            newComment: String,
        ): String {
            return when (commentMode) {
                ExternalActionCommentMode.SET -> newComment
                ExternalActionCommentMode.APPEND -> oldComment + newComment
                ExternalActionCommentMode.PREFIX -> newComment + oldComment
            }
        }

        suspend fun changeCurrent(): Boolean {
            var wasChanged = false
            runningRecordInteractor.getAll().let { allRecords ->
                if (typeId != null) allRecords.filter { it.id == typeId } else allRecords
            }.forEach { record ->
                record.copy(
                    comment = processComment(
                        oldComment = record.comment,
                        newComment = comment.orEmpty(),
                    ),
                ).let { runningRecordInteractor.add(it) }
                wasChanged = true
            }
            return wasChanged
        }

        suspend fun changeLast() {
            recordInteractor.getAllPrev(System.currentTimeMillis()).let { allRecords ->
                if (typeId != null) allRecords.filter { it.id == typeId } else allRecords
            }.forEach { record ->
                record.copy(
                    comment = processComment(
                        oldComment = record.comment,
                        newComment = comment.orEmpty(),
                    ),
                ).let { recordInteractor.add(it) }
            }
            recordsUpdateInteractor.send()
        }

        when (findMode) {
            ExternalActionFindRecordMode.CURRENT_OR_LAST -> {
                val currentWasChanged = changeCurrent()
                if (!currentWasChanged) changeLast()
            }
            ExternalActionFindRecordMode.CURRENT -> changeCurrent()
            ExternalActionFindRecordMode.LAST -> changeLast()
        }
    }

    private suspend fun getTypeIdByName(name: String): Long? {
        return recordTypeInteractor.getAll().firstOrNull { it.name == name }?.id
    }

    private suspend fun findTagIdByName(
        names: List<String>,
        typeId: Long,
    ): List<Long> {
        if (names.isEmpty()) return emptyList()
        return getSelectableTagsInteractor.execute(typeId)
            .filter { it.name in names && !it.archived }
            .map { it.id }
            .orEmpty()
    }

    /**
     * Supported formats:
     * [dateTimeFormat],
     * UTC timestamp in milliseconds.
     */
    private fun parseTimestamp(timeString: String): Long? {
        return parseDateTime(timeString)
            ?: timeString.toLongOrNull()
    }

    private fun parseDateTime(timeString: String): Long? {
        return synchronized(dateTimeFormat) {
            runCatching {
                dateTimeFormat.parse(timeString)
            }.getOrNull()?.time
        }
    }

    companion object {
        private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    }
}