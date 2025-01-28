package com.example.util.simpletimetracker.feature_records.interactor

import com.example.util.simpletimetracker.core.interactor.GetRunningRecordViewDataMediator
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.base.UNTRACKED_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.dropSeconds
import com.example.util.simpletimetracker.domain.extension.toRange
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.daysOfWeek.model.count
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_records.customView.RecordsCalendarViewData
import com.example.util.simpletimetracker.feature_records.mapper.RecordsViewDataMapper
import com.example.util.simpletimetracker.feature_records.model.RecordsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Long.min
import java.util.Calendar
import java.util.Comparator
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

class RecordsViewDataInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val getUntrackedRecordsInteractor: GetUntrackedRecordsInteractor,
    private val recordsViewDataMapper: RecordsViewDataMapper,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val getRunningRecordViewDataMediator: GetRunningRecordViewDataMediator,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
) {

    suspend fun getViewData(
        shift: Int,
        forSharing: Boolean,
    ): RecordsState = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        val showUntrackedInRecords = prefsInteractor.getShowUntrackedInRecords()
        val reverseOrder = prefsInteractor.getReverseOrderInCalendar()
        val recordTypes = recordTypeInteractor.getAll().associateBy(RecordType::id)
        val recordTypeIdsFiltered = prefsInteractor.getFilteredTypesOnList()
        val recordTags = recordTagInteractor.getAll()
        val goals = recordTypeGoalInteractor.getAllTypeGoals().groupBy { it.idData.value }
        val runningRecords = runningRecordInteractor.getAll()
        val isCalendarView = prefsInteractor.getShowRecordsCalendar()
        val calendarDayCount = prefsInteractor.getDaysInCalendar().count
        val daysCountInShift = if (isCalendarView) calendarDayCount else 1

        return@withContext (daysCountInShift - 1 downTo 0).map { dayInShift ->
            val actualShift = calendarToListShiftMapper.mapCalendarToListShift(
                calendarShift = shift,
                calendarDayCount = daysCountInShift,
            ).end - dayInShift

            val range = timeMapper.getRangeStartAndEnd(
                rangeLength = RangeLength.Day,
                shift = actualShift,
                firstDayOfWeek = DayOfWeek.MONDAY, // Doesn't matter for days.
                startOfDayShift = startOfDayShift,
            )
            val records = recordInteractor.getFromRange(range)

            val data = getRecordsViewData(
                records = records,
                runningRecords = runningRecords,
                recordTypes = recordTypes,
                recordTypeIdsFiltered = recordTypeIdsFiltered,
                recordTags = recordTags,
                goals = goals,
                range = range,
                isDarkTheme = isDarkTheme,
                useMilitaryTime = useMilitaryTime,
                useProportionalMinutes = useProportionalMinutes,
                showUntrackedInRecords = showUntrackedInRecords,
                showSeconds = showSeconds,
            )

            ViewDataIntermediate(
                rangeStart = range.timeStarted,
                rangeEnd = range.timeEnded,
                records = data,
            )
        }.let { data ->
            if (isCalendarView) {
                mapCalendarData(
                    data = data,
                    calendar = calendar,
                    startOfDayShift = startOfDayShift,
                    shift = shift,
                    reverseOrder = reverseOrder,
                    showSeconds = showSeconds,
                )
            } else {
                mapRecordsData(
                    data = data,
                    shift = shift,
                    forSharing = forSharing,
                )
            }
        }
    }

    private fun mapCalendarData(
        data: List<ViewDataIntermediate>,
        calendar: Calendar,
        startOfDayShift: Long,
        shift: Int,
        reverseOrder: Boolean,
        showSeconds: Boolean,
    ): RecordsState.CalendarData.Data {
        val currentTime = if (shift == 0) {
            timeMapper.mapFromStartOfDay(
                timeStamp = System.currentTimeMillis(),
                calendar = calendar,
            ) - startOfDayShift
        } else {
            null
        }
        val shouldMapLegends = data.size > 1

        return data
            .map { column ->
                val legend = if (shouldMapLegends) {
                    timeMapper.getDayOfWeek(
                        timestamp = column.rangeStart,
                        calendar = calendar,
                        startOfDayShift = startOfDayShift,
                    ).let(timeMapper::toShortDayOfWeekName)
                } else {
                    ""
                }

                val points = column.records.map { record ->
                    mapToCalendarPoint(
                        holder = record,
                        calendar = calendar,
                        startOfDayShift = startOfDayShift,
                        rangeStart = column.rangeStart,
                        rangeEnd = column.rangeEnd,
                        showSeconds = showSeconds,
                    )
                }

                RecordsCalendarViewData.Points(
                    legend = legend,
                    data = points,
                )
            }
            .let { list ->
                RecordsCalendarViewData(
                    currentTime = currentTime,
                    startOfDayShift = startOfDayShift,
                    points = list,
                    reverseOrder = reverseOrder,
                    shouldDrawTopLegends = shouldMapLegends,
                )
            }
            .let(RecordsState.CalendarData::Data)
    }

    private suspend fun mapRecordsData(
        data: List<ViewDataIntermediate>,
        shift: Int,
        forSharing: Boolean,
    ): RecordsState.RecordsData {
        val records = data.firstOrNull()?.records.orEmpty()

        val showFirstEnterHint = when {
            // Show hint only on current date.
            shift != 0 -> false
            // Check all records only if there is no records for this day.
            records.isNotEmpty() -> false
            // Try to find if any record exists.
            else -> recordInteractor.isEmpty() && runningRecordInteractor.isEmpty()
        }

        val hint = if (!forSharing) {
            recordsViewDataMapper.mapToHint()
        } else {
            null
        }

        val sortComparator: Comparator<RecordHolder> = compareByDescending<RecordHolder> {
            it.timeStartedTimestamp
        }.thenBy {
            // Otherwise 0 duration activities would be on top of untracked.
            it.data.typeId != UNTRACKED_ITEM_ID
        }

        val items = when {
            showFirstEnterHint -> listOf(recordViewDataMapper.mapToNoRecords())
            records.isEmpty() -> listOf(recordViewDataMapper.mapToEmpty())
            else -> {
                records
                    .sortedWith(sortComparator)
                    .map { it.data.value } +
                    listOfNotNull(hint)
            }
        }

        return RecordsState.RecordsData(items)
    }

    private suspend fun getRecordsViewData(
        records: List<Record>,
        runningRecords: List<RunningRecord>,
        recordTypes: Map<Long, RecordType>,
        recordTypeIdsFiltered: List<Long>,
        recordTags: List<RecordTag>,
        goals: Map<Long, List<RecordTypeGoal>>,
        range: Range,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        useProportionalMinutes: Boolean,
        showUntrackedInRecords: Boolean,
        showSeconds: Boolean,
    ): List<RecordHolder> {
        val trackedRecordsData = records
            .mapNotNull { record ->
                recordsViewDataMapper.map(
                    record = record,
                    recordType = recordTypes[record.typeId] ?: return@mapNotNull null,
                    recordTags = recordTags.filter { it.id in record.tagIds },
                    range = range,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(
                            value = it,
                            typeId = record.typeId,
                        ),
                    )
                }
            }

        val runningRecordsData = runningRecords
            .let {
                rangeMapper.getRunningRecordsFromRange(it, range)
            }
            .mapNotNull { runningRecord ->
                getRunningRecordViewDataMediator.execute(
                    type = recordTypes[runningRecord.id] ?: return@mapNotNull null,
                    tags = recordTags.filter { it.id in runningRecord.tagIds },
                    goals = goals[runningRecord.id].orEmpty(),
                    record = runningRecord,
                    nowIconVisible = true,
                    goalsVisible = false,
                    totalDurationVisible = false,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = runningRecord.timeStarted,
                        data = RecordHolder.Data.RunningRecordData(
                            value = it,
                            typeId = runningRecord.id,
                        ),
                    )
                }
            }

        val untrackedRecordsData = if (
            showUntrackedInRecords &&
            UNTRACKED_ITEM_ID !in recordTypeIdsFiltered
        ) {
            val recordRanges = records.map(Record::toRange)
            val runningRecordRanges = runningRecords.map(RunningRecord::toRange)
            getUntrackedRecordsInteractor.get(
                range = range,
                records = recordRanges + runningRecordRanges,
            ).map { untrackedRecord ->
                recordsViewDataMapper.mapToUntracked(
                    record = untrackedRecord,
                    range = range,
                    isDarkTheme = isDarkTheme,
                    useMilitaryTime = useMilitaryTime,
                    useProportionalMinutes = useProportionalMinutes,
                    showSeconds = showSeconds,
                ).let {
                    RecordHolder(
                        timeStartedTimestamp = it.timeStartedTimestamp,
                        data = RecordHolder.Data.RecordData(
                            value = it,
                            typeId = UNTRACKED_ITEM_ID,
                        ),
                    )
                }
            }
        } else {
            emptyList()
        }

        return (trackedRecordsData + runningRecordsData + untrackedRecordsData)
            .filter { it.data.typeId !in recordTypeIdsFiltered }
    }

    private fun mapToCalendarPoint(
        holder: RecordHolder,
        calendar: Calendar,
        startOfDayShift: Long,
        rangeStart: Long,
        rangeEnd: Long,
        showSeconds: Boolean,
    ): RecordsCalendarViewData.Point {
        // Record data already clamped.
        val timeStartedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData ->
                holder.timeStartedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is RecordHolder.Data.RunningRecordData ->
                max(holder.timeStartedTimestamp, rangeStart)
        }
        val timeEndedTimestamp = when (holder.data) {
            is RecordHolder.Data.RecordData ->
                holder.data.value.timeEndedTimestamp.let { if (showSeconds) it else it.dropSeconds() }
            is RecordHolder.Data.RunningRecordData ->
                min(System.currentTimeMillis(), rangeEnd)
        }

        val start = timeMapper.mapFromStartOfDay(
            // Normalize to set start of day correctly.
            timeStamp = timeStartedTimestamp - startOfDayShift,
            calendar = calendar,
        ) + startOfDayShift

        val duration = (timeEndedTimestamp - timeStartedTimestamp)
            // Otherwise would be invisible.
            .takeUnless { it == 0L } ?: minuteInMillis

        val end = start + duration

        return RecordsCalendarViewData.Point(
            start = start - startOfDayShift,
            end = end - startOfDayShift,
            data = when (holder.data) {
                is RecordHolder.Data.RecordData -> {
                    RecordsCalendarViewData.Point.Data.RecordData(holder.data.value)
                }
                is RecordHolder.Data.RunningRecordData -> {
                    RecordsCalendarViewData.Point.Data.RunningRecordData(holder.data.value)
                }
            },
        )
    }

    private data class RecordHolder(
        val timeStartedTimestamp: Long,
        val data: Data,
    ) {
        sealed interface Data {
            val value: ViewHolderType
            val typeId: Long

            data class RecordData(
                override val value: RecordViewData,
                override val typeId: Long,
            ) : Data

            data class RunningRecordData(
                override val value: RunningRecordViewData,
                override val typeId: Long,
            ) : Data
        }
    }

    private data class ViewDataIntermediate(
        val rangeStart: Long,
        val rangeEnd: Long,
        val records: List<RecordHolder>,
    )

    companion object {
        private val minuteInMillis = TimeUnit.MINUTES.toMillis(1)
    }
}