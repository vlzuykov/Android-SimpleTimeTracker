package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsMergeTest : BaseUiTest() {

    @Test
    fun mergeVisibility() {
        val name = "Name"

        // Setup
        testUtils.addActivity(name)
        testUtils.addRecord(name)
        calendar.timeInMillis = System.currentTimeMillis()
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
            timeEnded = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
        )
        testUtils.addRunningRecord(name)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        Thread.sleep(1000)

        // Running record - not shown
        tryAction {
            longClickOnView(
                allOf(withId(baseR.id.viewRunningRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
            )
        }
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        checkViewDoesNotExist(withText(coreR.string.change_record_merge))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Record - not shown
        NavUtils.openRecordsScreen()
        clickOnView(
            allOf(withId(baseR.id.viewRecordItem), hasDescendant(withText(name)), isCompletelyDisplayed()),
        )
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        checkViewDoesNotExist(withText(coreR.string.change_record_merge))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // New record - not shown
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        checkViewDoesNotExist(withText(coreR.string.change_record_merge))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()

        // Untracked and have prev record - shown
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_merge)),
        )
        checkViewIsDisplayed(withText(coreR.string.change_record_merge))
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        pressBack()
    }

    @Test
    fun merge() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val timeStartedTimestamp = current - TimeUnit.MINUTES.toMillis(15)
        val timeEndedTimestamp = current - TimeUnit.MINUTES.toMillis(5)
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp).formatInterval()
        val untrackedRangePreview = (current - timeEndedTimestamp).formatInterval()
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, timeStarted = timeStartedTimestamp, timeEnded = timeEndedTimestamp)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        // Check records
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordItem),
                    hasDescendant(withText(coreR.string.untracked_time_name)),
                    hasDescendant(withText(current.formatTime())),
                    hasDescendant(withText(timeEndedTimestamp.formatTime())),
                    hasDescendant(withText(untrackedRangePreview)),
                    isCompletelyDisplayed(),
                ),
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedTimestamp.formatTime())),
                hasDescendant(withText(timeEndedTimestamp.formatTime())),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed(),
            ),
        )

        // Merge
        clickOnView(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText(untrackedRangePreview)),
                isCompletelyDisplayed(),
            ),
        )
        onView(withText(coreR.string.change_record_actions_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_actions_hint)
        scrollRecyclerToView(
            changeRecordR.id.rvChangeRecordAction,
            hasDescendant(withText(coreR.string.change_record_merge)),
        )
        clickOnViewWithText(coreR.string.change_record_merge)

        // Check records
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                hasDescendant(withText(untrackedRangePreview)),
                isCompletelyDisplayed(),
            ),
        )
        timeRangePreview = (current - timeStartedTimestamp).formatInterval()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedTimestamp.formatTime())),
                hasDescendant(withText(current.formatTime())),
                hasDescendant(withText(timeRangePreview)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun fromQuickActions() {
        val name = "Name"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val timeStartedTimestamp = current - TimeUnit.MINUTES.toMillis(15)
        val timeEndedTimestamp = current - TimeUnit.MINUTES.toMillis(5)
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, timeStarted = timeStartedTimestamp, timeEnded = timeEndedTimestamp)
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        NavUtils.openRecordsScreen()

        // Check records
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordItem),
                    hasDescendant(withText(coreR.string.untracked_time_name)),
                    isCompletelyDisplayed(),
                ),
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Merge
        longClickOnView(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )
        clickOnViewWithText(coreR.string.change_record_merge)

        // Check records
        checkViewDoesNotExist(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(coreR.string.untracked_time_name)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(timeStartedTimestamp.formatTime())),
                hasDescendant(withText(current.formatTime())),
                isCompletelyDisplayed(),
            ),
        )
    }
}
