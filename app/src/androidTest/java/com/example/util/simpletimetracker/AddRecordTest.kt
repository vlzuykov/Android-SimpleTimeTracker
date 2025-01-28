package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddRecordTest : BaseUiTest() {

    @Test
    fun addRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Some_comment"
        val tag1 = "Tag1"
        val tag2 = "Tag2"

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordStatistics))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordType))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordCategories))
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.getFormattedDateTime(
            time = currentTime - 60 * 60 * 1000, useMilitaryTime = true, showSeconds = false,
        )
        var timeEnded = timeMapper.getFormattedDateTime(
            time = currentTime, useMilitaryTime = true, showSeconds = false,
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedDate), withText(timeStarted.date)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withText(timeStarted.time)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeEndedDate), withText(timeEnded.date)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeEndedTime), withText(timeEnded.time)),
        )

        // Set time started
        val hourStarted = 15
        val minutesStarted = 16
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        val timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp.let {
            timeMapper.getFormattedDateTime(time = it, useMilitaryTime = true, showSeconds = false)
        }
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedDate), withText(timeStarted.date)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withText(timeStarted.time)),
        )

        // Set time ended
        val hourEnded = 17
        val minutesEnded = 19
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        val timeEndedTimestamp = Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, hourEnded)
            set(Calendar.MINUTE, minutesEnded)
            timeInMillis
        }
        timeEnded = timeEndedTimestamp.let {
            timeMapper.getFormattedDateTime(time = it, useMilitaryTime = true, showSeconds = false)
        }
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeEndedDate), withText(timeEnded.date)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeEndedTime), withText(timeEnded.time)),
        )

        // Preview is updated
        val timeStartedPreview = timeStartedTimestamp.let {
            timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false)
        }
        val timeEndedPreview = timeEndedTimestamp.let {
            timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false)
        }
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeEndedPreview)))
        checkPreviewUpdated(hasDescendant(withText("2$hourString 3$minuteString")))

        // Activity not selected
        clickOnViewWithText(coreR.string.change_record_save)

        // Open activity chooser
        clickOnViewWithText(coreR.string.change_record_type_field)
        checkViewIsDisplayed(withId(changeRecordR.id.rvChangeRecordType))

        // Selecting activity
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(hasDescendant(withTag(icon)))

        // Open tag chooser
        tryAction { checkViewIsDisplayed(withId(changeRecordR.id.rvChangeRecordCategories)) }

        // Selecting tags
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag1")))

        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag1, $tag2")))

        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag2")))

        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2))
        checkPreviewUpdated(hasDescendant(withText(name)))

        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag1))
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2))
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordCategory)

        // Set comment
        clickOnViewWithText(coreR.string.change_record_comment_field)
        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, comment)
        closeSoftKeyboard()
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment))) }
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Save
        clickOnViewWithText(coreR.string.change_record_save)

        // Record added
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(changeRecordR.id.viewRecordItem),
                    withCardColor(color),
                    hasDescendant(withText("$name - $tag1, $tag2")),
                    hasDescendant(withTag(icon)),
                    hasDescendant(withText(timeStartedPreview)),
                    hasDescendant(withText(timeEndedPreview)),
                    hasDescendant(withText("2$hourString 3$minuteString")),
                    hasDescendant(withText(comment)),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }

    @Test
    fun addRecordTypesEmpty() {
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Open activity chooser
        clickOnViewWithText(coreR.string.change_record_type_field)
        checkViewIsDisplayed(withText(coreR.string.record_types_empty))
    }

    @Test
    fun addRecordTagsEmpty() {
        val name = "name"
        testUtils.addActivity(name)

        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Select activity
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Open tag chooser
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordCategory)
        checkViewIsDisplayed(withText(coreR.string.categories_record_hint))
    }

    @Test
    fun addRecordComment() {
        val nameNoComments = "Name1"
        val nameComment = "Name2"
        val nameComments = "Name3"
        val comment1 = "Comment1"
        val comment2 = "Comment2"
        val comment3 = "Comment3"

        // Add data
        testUtils.addActivity(nameNoComments)
        testUtils.addActivity(nameComment)
        testUtils.addActivity(nameComments)
        testUtils.addRecord(nameNoComments)
        testUtils.addRecord(nameComment, comment = comment1)
        testUtils.addRecord(nameComments, comment = comment2)
        testUtils.addRecord(nameComments, comment = comment3)

        // Check comments
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // No last comments
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with no previous comments
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameNoComments))

        // Still no last comments
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with one previous comment
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameComment))

        // One last comment
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewIsDisplayed(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment1)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment1))) }
        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, "")
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with many previous comments
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameComments))

        // Two last comments
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment2)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment2))) }
        clickOnViewWithText(comment3)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment3))) }
    }

    @Test
    fun favouriteComments() {
        val name = "name"
        val comment = "comment"
        val comment1 = "favourite comment1"
        val comment2 = "favourite comment2"

        // Add data
        testUtils.addActivity(name)
        testUtils.addRecord(typeName = name, comment = comment)

        // Check
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // No favourites
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordFavouriteComment))

        // Add favourite
        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, comment1)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment1))) }
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordFavouriteComment))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordFavouriteComment)
        checkViewIsDisplayed(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment1)))

        // Add another
        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, comment2)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment2))) }
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordFavouriteComment))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordFavouriteComment)
        checkViewIsDisplayed(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment1)))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment2)))

        // Favourite click
        clickOnViewWithText(comment1)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment1))) }

        // Remove favourite
        clickOnViewWithId(changeRecordR.id.btnChangeRecordFavouriteComment)
        checkViewIsDisplayed(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewDoesNotExist(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment1)))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment2)))

        // Favourites and last
        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, "")
        clickOnViewWithText(coreR.string.change_record_comment_field)
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewIsDisplayed(withText(comment))
        checkViewIsDisplayed(withText(coreR.string.change_record_favourite_comments_hint))
        checkViewIsDisplayed(withText(comment2))
    }

    @Test
    fun searchComments() {
        val name = "name"
        val comment1 = "comment"
        val comment2 = "another comment"

        // Add data
        testUtils.addActivity(name)
        testUtils.addRecord(name, comment = comment1)
        testUtils.addRecord(name, comment = comment2)

        // Check
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()

        checkViewIsDisplayed(withId(changeRecordR.id.etChangeRecordCommentField))

        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, "comm")
        tryAction {
            checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment1)))
        }
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment2)))

        typeTextIntoView(changeRecordR.id.etChangeRecordCommentField, "another")
        tryAction {
            checkViewDoesNotExist(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment1)))
        }
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment2)))

        // Click on search result
        clickOnView(allOf(withId(changeRecordR.id.tvChangeRecordItemComment), withText(comment2)))
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment2))) }
    }

    @Test
    fun addRecordAdjustTime() {
        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Setup
        val hourStarted = 15
        val minutesStarted = 0
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        val hourEnded = 16
        val minutesEnded = 0
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(
            timeStarted = "15:00", timeEnded = "16:00", duration = "1$hourString 0$minuteString",
        )

        // Check visibility
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeStartedAdjust))
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeEndedAdjust))

        fun adjust(
            isStart: Boolean,
            buttonText: String,
        ) {
            val containerId = if (isStart) {
                changeRecordR.id.containerChangeRecordTimeStartedAdjust
            } else {
                changeRecordR.id.containerChangeRecordTimeEndedAdjust
            }
            clickOnView(allOf(isDescendantOfA(withId(containerId)), withText(buttonText)))
        }

        // Check time start adjustments
        adjust(isStart = true, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "14:30", timeEnded = "16:00", duration = "1$hourString 30$minuteString",
        )
        adjust(isStart = true, buttonText = "-5")
        checkAfterTimeAdjustment(
            timeStarted = "14:25", timeEnded = "16:00", duration = "1$hourString 35$minuteString",
        )
        adjust(isStart = true, buttonText = "-1")
        checkAfterTimeAdjustment(
            timeStarted = "14:24", timeEnded = "16:00", duration = "1$hourString 36$minuteString",
        )
        adjust(isStart = true, buttonText = "+1")
        checkAfterTimeAdjustment(
            timeStarted = "14:25", timeEnded = "16:00", duration = "1$hourString 35$minuteString",
        )
        adjust(isStart = true, buttonText = "+5")
        checkAfterTimeAdjustment(
            timeStarted = "14:30", timeEnded = "16:00", duration = "1$hourString 30$minuteString",
        )
        adjust(isStart = true, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "15:00", timeEnded = "16:00", duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = true, buttonText = "+30")
        adjust(isStart = true, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "16:00", duration = "0$minuteString",
        )

        // Check time end adjustments
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:00", duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = false, buttonText = "+5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:05", duration = "1$hourString 5$minuteString",
        )
        adjust(isStart = false, buttonText = "+1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:06", duration = "1$hourString 6$minuteString",
        )
        adjust(isStart = false, buttonText = "-1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:05", duration = "1$hourString 5$minuteString",
        )
        adjust(isStart = false, buttonText = "-5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:00", duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = false, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "16:30", duration = "30$minuteString",
        )
        adjust(isStart = false, buttonText = "-30")
        adjust(isStart = false, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "15:30", timeEnded = "15:30", duration = "0$minuteString",
        )
    }

    @Test
    fun adjustDurationVisibility() {
        fun checkField(
            isStart: Boolean,
            isDuration: Boolean,
        ) {
            val textField = if (isStart) {
                changeRecordR.id.tvChangeRecordTimeStartedAdjust
            } else {
                changeRecordR.id.tvChangeRecordTimeEndedAdjust
            }
            val text = if (isDuration) {
                coreR.string.change_record_date_time_duration
            } else {
                if (isStart) {
                    coreR.string.change_record_date_time_start
                } else {
                    coreR.string.change_record_date_time_end
                }
            }
            val dateField = if (isStart) {
                changeRecordR.id.tvChangeRecordTimeStartedDate
            } else {
                changeRecordR.id.tvChangeRecordTimeEndedDate
            }
            val timeField = if (isStart) {
                changeRecordR.id.tvChangeRecordTimeStartedTime
            } else {
                changeRecordR.id.tvChangeRecordTimeEndedTime
            }

            checkViewIsDisplayed(allOf(withId(textField), withText(text)))
            if (isDuration) {
                checkViewIsNotDisplayed(withId(dateField))
                checkViewIsDisplayed(withId(timeField))
            } else {
                checkViewIsDisplayed(withId(dateField))
                checkViewIsDisplayed(withId(timeField))
            }
        }

        // Switch start and end
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        checkField(isStart = true, isDuration = false)
        checkField(isStart = false, isDuration = false)

        // Check start
        clickOnViewWithText(coreR.string.change_record_date_time_start)
        checkField(isStart = true, isDuration = true)
        checkField(isStart = false, isDuration = false)
        clickOnViewWithText(coreR.string.change_record_date_time_duration)
        checkField(isStart = true, isDuration = false)
        checkField(isStart = false, isDuration = false)

        // Check end
        clickOnViewWithText(coreR.string.change_record_date_time_end)
        checkField(isStart = true, isDuration = false)
        checkField(isStart = false, isDuration = true)
        clickOnViewWithText(coreR.string.change_record_date_time_duration)
        checkField(isStart = true, isDuration = false)
        checkField(isStart = false, isDuration = false)

        // Check from start to end
        clickOnViewWithText(coreR.string.change_record_date_time_start)
        checkField(isStart = true, isDuration = true)
        checkField(isStart = false, isDuration = false)
        clickOnViewWithText(coreR.string.change_record_date_time_end)
        checkField(isStart = true, isDuration = false)
        checkField(isStart = false, isDuration = true)
    }

    @Test
    fun adjustDuration() {
        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)

        // Setup
        val hourStarted = 15
        val minutesStarted = 0
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        val hourEnded = 16
        val minutesEnded = 0
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(
            timeStarted = "15:00", timeEnded = "16:00", duration = "1$hourString 0$minuteString",
        )

        // Check visibility
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeStartedAdjust))
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeEndedAdjust))

        fun adjust(
            isStart: Boolean,
            buttonText: String,
        ) {
            val containerId = if (isStart) {
                changeRecordR.id.containerChangeRecordTimeStartedAdjust
            } else {
                changeRecordR.id.containerChangeRecordTimeEndedAdjust
            }
            clickOnView(allOf(isDescendantOfA(withId(containerId)), withText(buttonText)))
        }

        // Check time start adjustments
        clickOnViewWithText(changeRecordR.string.change_record_date_time_start)
        adjust(isStart = true, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "15:30",
            timeEnded = "16:00",
            timeStartedField = "30$minuteString",
            duration = "30$minuteString",
        )
        adjust(isStart = true, buttonText = "-5")
        checkAfterTimeAdjustment(
            timeStarted = "15:35",
            timeEnded = "16:00",
            timeStartedField = "25$minuteString",
            duration = "25$minuteString",
        )
        adjust(isStart = true, buttonText = "-1")
        checkAfterTimeAdjustment(
            timeStarted = "15:36",
            timeEnded = "16:00",
            timeStartedField = "24$minuteString",
            duration = "24$minuteString",
        )
        adjust(isStart = true, buttonText = "+1")
        checkAfterTimeAdjustment(
            timeStarted = "15:35",
            timeEnded = "16:00",
            timeStartedField = "25$minuteString",
            duration = "25$minuteString",
        )
        adjust(isStart = true, buttonText = "+5")
        checkAfterTimeAdjustment(
            timeStarted = "15:30",
            timeEnded = "16:00",
            timeStartedField = "30$minuteString",
            duration = "30$minuteString",
        )
        adjust(isStart = true, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "15:00",
            timeEnded = "16:00",
            timeStartedField = "1$hourString 0$minuteString",
            duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = true, buttonText = "-30")
        adjust(isStart = true, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:00",
            timeStartedField = "0$minuteString",
            duration = "0$minuteString",
        )
        adjust(isStart = true, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:00",
            timeStartedField = "0$minuteString",
            duration = "0$minuteString",
        )
        adjust(isStart = true, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "15:30",
            timeEnded = "16:00",
            timeStartedField = "30$minuteString",
            duration = "30$minuteString",
        )
        adjust(isStart = true, buttonText = "0")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:00",
            timeStartedField = "0$minuteString",
            duration = "0$minuteString",
        )

        // Check time end adjustments
        clickOnViewWithText(changeRecordR.string.change_record_date_time_end)
        adjust(isStart = false, buttonText = "+30")
        adjust(isStart = false, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "17:00",
            timeEndedField = "1$hourString 0$minuteString",
            duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = false, buttonText = "+5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "17:05",
            timeEndedField = "1$hourString 5$minuteString",
            duration = "1$hourString 5$minuteString",
        )
        adjust(isStart = false, buttonText = "+1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "17:06",
            timeEndedField = "1$hourString 6$minuteString",
            duration = "1$hourString 6$minuteString",
        )
        adjust(isStart = false, buttonText = "-1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "17:05",
            timeEndedField = "1$hourString 5$minuteString",
            duration = "1$hourString 5$minuteString",
        )
        adjust(isStart = false, buttonText = "-5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "17:00",
            timeEndedField = "1$hourString 0$minuteString",
            duration = "1$hourString 0$minuteString",
        )
        adjust(isStart = false, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:30",
            timeEndedField = "30$minuteString",
            duration = "30$minuteString",
        )
        adjust(isStart = false, buttonText = "-30")
        adjust(isStart = false, buttonText = "-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:00",
            timeEndedField = "0$minuteString",
            duration = "0$minuteString",
        )
        adjust(isStart = false, buttonText = "+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:30",
            timeEndedField = "30$minuteString",
            duration = "30$minuteString",
        )
        adjust(isStart = false, buttonText = "0")
        checkAfterTimeAdjustment(
            timeStarted = "16:00",
            timeEnded = "16:00",
            timeEndedField = "0$minuteString",
            duration = "0$minuteString",
        )
    }

    private fun checkAfterTimeAdjustment(
        timeStarted: String,
        timeEnded: String,
        timeStartedField: String = timeStarted,
        timeEndedField: String = timeEnded,
        duration: String,
    ) {
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRecordItemTimeStarted), withText(timeStarted))),
        )
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRecordItemTimeFinished), withText(timeEnded))),
        )
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRecordItemDuration), withText(duration))),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withSubstring(timeStartedField)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.tvChangeRecordTimeEndedTime), withSubstring(timeEndedField)),
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), matcher))
}
