package com.example.util.simpletimetracker

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
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordActionsChangeTagTest : BaseUiTest() {

    @Test
    fun visibility() {
        val recordWithTags = "recordWithTags"
        val recordWithNoTags = "recordWithNoTags"
        val runningWithTags = "runningWithTags"
        val runningWithNoTags = "runningWithNoTags"
        val recordTag = "recordTag"
        val runningTag = "runningTag"

        // Setup
        testUtils.addActivity(recordWithTags)
        testUtils.addActivity(recordWithNoTags)
        testUtils.addActivity(runningWithTags)
        testUtils.addActivity(runningWithNoTags)
        testUtils.addRecordTag(recordTag, typeName = recordWithTags)
        testUtils.addRecordTag(runningTag, typeName = runningWithTags)
        testUtils.addRunningRecord(runningWithTags)
        testUtils.addRunningRecord(runningWithNoTags)
        testUtils.addRecord(recordWithTags)
        testUtils.addRecord(recordWithNoTags)
        calendar.timeInMillis = System.currentTimeMillis()
        testUtils.addRecord(
            typeName = recordWithTags,
            timeStarted = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
            timeEnded = calendar.timeInMillis - TimeUnit.DAYS.toMillis(1),
        )
        Thread.sleep(1000)

        // Running record with tags - shown
        NavUtils.openRecordsScreen()
        longClickOnView(allOf(withText(runningWithTags), isCompletelyDisplayed()))
        checkViewIsDisplayed(withText(coreR.string.data_edit_change_tag))
        pressBack()

        // Running records with no tags - not shown
        NavUtils.openRecordsScreen()
        longClickOnView(allOf(withText(runningWithNoTags), isCompletelyDisplayed()))
        checkViewDoesNotExist(withText(coreR.string.data_edit_change_tag))
        pressBack()

        // Record with tags - shown
        longClickOnView(allOf(withText(recordWithTags), isCompletelyDisplayed()))
        checkViewIsDisplayed(withText(coreR.string.data_edit_change_tag))
        pressBack()

        // Record with no tags - not shown
        longClickOnView(allOf(withText(recordWithNoTags), isCompletelyDisplayed()))
        checkViewDoesNotExist(withText(coreR.string.data_edit_change_tag))
        pressBack()

        // Untracked - not shown
        longClickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewDoesNotExist(withText(coreR.string.data_edit_change_tag))
    }

    @Test
    fun record() {
        val name1 = "Name1"
        val color1 = firstColor
        val icon1 = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val tagGeneral = "TagGeneral"
        val fullName1 = "$name1 - $tag, $tagGeneral"
        val fullName2 = "$name1 - $tagGeneral"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val difference = TimeUnit.MINUTES.toMillis(30)
        val timeStartedTimestamp = current - difference
        val timeStartedPreview = timeStartedTimestamp.formatTime()
        val timeEndedPreview = current.formatTime()
        val timeRangePreview = difference.formatInterval()

        testUtils.addActivity(name = name1, color = color1, icon = icon1)
        testUtils.addRecordTag(tag, typeName = name1)
        testUtils.addRecordTag(tagGeneral)
        testUtils.addRecord(
            typeName = name1,
            timeStarted = timeStartedTimestamp,
            timeEnded = current,
            tagNames = listOf(tag, tagGeneral),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        checkRecord(
            name = fullName1,
            color = color1,
            icon = icon1,
            timeStartedPreview = timeStartedPreview,
            timeEndedPreview = timeEndedPreview,
            timeRangePreview = timeRangePreview,
            comment = comment,
        )

        // Change
        longClickOnView(allOf(withText(fullName1), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.data_edit_change_tag)
        clickOnViewWithText(tag)
        clickOnViewWithText(R.string.change_record_save)

        tryAction {
            checkRecord(
                name = fullName2,
                color = color1,
                icon = icon1,
                timeStartedPreview = timeStartedPreview,
                timeEndedPreview = timeEndedPreview,
                timeRangePreview = timeRangePreview,
                comment = comment,
            )
        }
    }

    @Test
    fun runningRecord() {
        val name1 = "Name1"
        val color1 = firstColor
        val icon1 = firstIcon
        val comment = "Some_comment"
        val tag = "Tag"
        val tagGeneral = "TagGeneral"
        val fullName1 = "$name1 - $tag, $tagGeneral"
        val fullName2 = "$name1 - $tagGeneral"
        val calendar = Calendar.getInstance()

        // Setup
        val current = calendar.timeInMillis
        val difference = TimeUnit.MINUTES.toMillis(30)
        val timeStartedTimestamp = current - difference
        val timeStartedPreview = timeStartedTimestamp.formatTime()

        testUtils.addActivity(name = name1, color = color1, icon = icon1)
        testUtils.addRecordTag(tag, typeName = name1)
        testUtils.addRecordTag(tagGeneral)
        testUtils.addRunningRecord(
            typeName = name1,
            timeStarted = timeStartedTimestamp,
            tagNames = listOf(tag, tagGeneral),
            comment = comment,
        )

        // Check record
        NavUtils.openRecordsScreen()
        checkRunningRecord(
            name = fullName1,
            color = color1,
            icon = icon1,
            timeStartedPreview = timeStartedPreview,
            comment = comment,
        )

        // Change
        longClickOnView(allOf(withText(fullName1), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.data_edit_change_tag)
        clickOnViewWithText(tag)
        clickOnViewWithText(R.string.change_record_save)

        tryAction {
            checkRunningRecord(
                name = fullName2,
                color = color1,
                icon = icon1,
                timeStartedPreview = timeStartedPreview,
                comment = comment,
            )
        }
    }

    @Suppress("SameParameterValue")
    private fun checkRecord(
        name: String,
        color: Int,
        icon: Int,
        timeStartedPreview: String,
        timeEndedPreview: String,
        timeRangePreview: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Suppress("SameParameterValue")
    private fun checkRunningRecord(
        name: String,
        color: Int,
        icon: Int,
        timeStartedPreview: String,
        comment: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed(),
            ),
        )
    }
}
