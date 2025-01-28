package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

object GoalsTestUtils {

    val durationInSeconds = TimeUnit.MINUTES.toSeconds(10)
    private val durationInMillis = TimeUnit.MINUTES.toMillis(10)

    fun getSessionDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Session, duration)

    fun getSessionDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Session, duration)

    fun getDailyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Daily, duration)

    fun getDailyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Daily, duration)

    fun getWeeklyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Weekly, duration)

    fun getWeeklyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Weekly, duration)

    fun getMonthlyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Monthly, duration)

    fun getMonthlyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Monthly, duration)

    fun getDailyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Daily, count)

    fun getDailyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Daily, count)

    fun getWeeklyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Weekly, count)

    fun getWeeklyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Weekly, count)

    fun getMonthlyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Monthly, count)

    fun getMonthlyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Monthly, count)

    fun addRecords(testUtils: TestUtils, typeName: String) {
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
        }.timeInMillis
        val thisWeek = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            val dateShift = when {
                get(Calendar.DAY_OF_WEEK) == firstDayOfWeek -> +1
                get(Calendar.DAY_OF_MONTH) == 1 -> +1
                else -> -1
            }
            add(Calendar.DATE, dateShift)
        }.timeInMillis
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            val dateShift = if (get(Calendar.DAY_OF_MONTH) < 15) +7 else -7
            add(Calendar.DATE, dateShift)
        }.timeInMillis

        testUtils.addRecord(
            typeName = typeName,
            timeStarted = currentTime - durationInMillis,
            timeEnded = currentTime,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisWeek - durationInMillis,
            timeEnded = thisWeek,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisMonth - durationInMillis,
            timeEnded = thisMonth,
        )
    }

    fun checkNoStatisticsGoal(typeName: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            isCompletelyDisplayed(),
        ).let(::checkViewDoesNotExist)
    }

    fun checkStatisticsGoal(
        typeName: String,
        current: String,
        goal: String,
    ) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemCurrent),
            withSubstring(current),
            isCompletelyDisplayed(),
        ).let(::checkViewIsDisplayed)

        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemGoal),
            withText(goal),
            isCompletelyDisplayed(),
        ).let(::checkViewIsDisplayed)
    }

    fun checkStatisticsPercent(
        typeName: String,
        percent: String,
    ) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemPercent),
            withText(percent),
        ).let(::checkViewIsDisplayed)
    }

    fun checkStatisticsMark(typeName: String, isVisible: Boolean) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.ivStatisticsGoalItemCheck),
        ).let {
            if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
        }
    }

    fun checkTypeMark(typeName: String, isVisible: Boolean) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheckOutline))
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheck))
            .let { if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it) }
    }

    fun checkNoTypeMark(typeName: String) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheckOutline))
            .let(::checkViewIsNotDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheck))
            .let(::checkViewIsNotDisplayed)
    }

    fun checkRunningGoal(typeName: String, goal: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvRunningRecordItemGoalTime),
            withSubstring(goal),
        ).let(::checkViewIsDisplayed)
    }

    fun checkNoRunningGoal(typeName: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvRunningRecordItemGoalTime),
        ).let(::checkViewIsNotDisplayed)
    }

    fun checkRunningMark(typeName: String, isVisible: Boolean) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.ivRunningRecordItemGoalTimeCheck),
        ).let {
            if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
        }
    }

    private fun getTypeMatcher(typeName: String): Matcher<View> {
        return isDescendantOfA(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(typeName)),
            ),
        )
    }

    private fun getDurationGoal(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            idData = RecordTypeGoal.IdData.Type(0),
            range = range,
            type = RecordTypeGoal.Type.Duration(duration),
            subtype = RecordTypeGoal.Subtype.Goal,
            daysOfWeek = DayOfWeek.entries.toSet(),
        )
    }

    private fun getDurationGoalCategory(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return getDurationGoal(range = range, duration = duration)
            .copy(idData = RecordTypeGoal.IdData.Category(0))
    }

    private fun getCountGoal(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            idData = RecordTypeGoal.IdData.Type(0),
            range = range,
            type = RecordTypeGoal.Type.Count(count),
            subtype = RecordTypeGoal.Subtype.Goal,
            daysOfWeek = DayOfWeek.entries.toSet(),
        )
    }

    private fun getCountGoalCategory(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return getCountGoal(range = range, count = count)
            .copy(idData = RecordTypeGoal.IdData.Category(0))
    }
}
