package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRecordTypeTest : BaseUiTest() {

    @Test
    fun changeRecordType() {
        val name = "Test"
        val newName = "Updated"

        // Add item
        NavUtils.addActivity(name, firstColor, firstIcon)

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeArchive))
        checkViewIsDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeDelete))
        checkViewIsDisplayed(withId(changeRecordTypeR.id.btnChangeRecordTypeStatistics))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(changeRecordTypeR.id.rvIconSelection))
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.etChangeRecordTypeName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(hasDescendant(withCardColor(firstColor)))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Check statistics navigation
        clickOnViewWithId(changeRecordTypeR.id.btnChangeRecordTypeStatistics)
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(name)),
            ),
        )
        pressBack()

        // Change item
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withCardColor(lastColor)))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )
        clickOnViewWithText(coreR.string.change_record_type_color_hint)

        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        onView(withId(changeRecordTypeR.id.rvIconSelection)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type updated
        tryAction { checkViewIsDisplayed(withText(newName)) }
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))

        // Change again
        longClickOnView(withText(newName))
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint),
            ),
        )
        clickOnViewWithText(firstEmoji)

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(firstEmoji)))

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type updated
        tryAction { checkViewIsDisplayed(withText(firstEmoji)) }
    }

    @Test
    fun runningRecordChangeRecordType() {
        val name = "name"
        val newName = "new name"

        // Add activity
        NavUtils.addActivity(name, firstColor, firstIcon)

        // Start timer
        tryAction { clickOnViewWithText(name) }
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.viewRunningRecordItem),
                withCardColor(firstColor),
                hasDescendant(withText(name)),
                hasDescendant(withTag(firstIcon)),
            ),
        )
        checkViewDoesNotExist(withSubstring(getString(coreR.string.change_record_type_session_goal_time).lowercase()))

        // Change activity
        longClickOnView(
            allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRecordTypeItem)), withText(name)),
        )

        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, newName)

        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnViewWithText(coreR.string.change_record_type_color_hint)

        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        onView(withId(changeRecordTypeR.id.rvIconSelection)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withTag(lastIcon))
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)

        openGoals()
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.layoutChangeRecordTypeGoalSession)),
                withId(changeRecordTypeR.id.fieldChangeRecordTypeGoalDuration),
            ),
        )
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        tryAction { checkViewIsDisplayed(withText("10$minuteString")) }
        openGoals()

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record running record updated
        tryAction {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRunningRecordItem)), withText(newName)),
            )
        }
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.viewRunningRecordItem), withCardColor(lastColor)),
        )
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(changeRecordTypeR.id.viewRunningRecordItem)), withTag(lastIcon)),
        )
        checkViewIsDisplayed(withSubstring(getString(coreR.string.change_record_type_session_goal_time).lowercase()))
    }

    @Test
    fun duplicate() {
        val type = "type"
        val category = "category"
        val note = "note"

        // Add data
        testUtils.addCategory(category)
        testUtils.addActivity(
            name = type,
            color = firstColor,
            icon = firstIcon,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(TimeUnit.MINUTES.toSeconds(1))),
            note = note,
            categories = listOf(category),
        )

        // Not visible on add
        clickOnViewWithText(coreR.string.running_records_add_type)
        closeSoftKeyboard()
        onView(withText(coreR.string.change_record_type_additional_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        checkViewIsNotDisplayed(withText(coreR.string.change_record_duplicate))
        pressBack()
        pressBack()

        // Duplicate
        longClickOnView(withText(type))
        onView(withText(coreR.string.change_record_type_additional_hint)).perform(nestedScrollTo())
        clickOnViewWithText(coreR.string.change_record_type_additional_hint)
        clickOnViewWithText(coreR.string.change_record_duplicate)

        // Check
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordTypeItem),
                    hasDescendant(withText(type)),
                    hasDescendant(withTag(firstIcon)),
                    hasDescendant(withCardColor(firstColor)),
                ),
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText("$type (2)")),
                hasDescendant(withTag(firstIcon)),
                hasDescendant(withCardColor(firstColor)),
            ),
        )
        longClickOnView(withText("$type (2)"))
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeCategoryPreview),
                withText("1"),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalPreview),
                withText("1"),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.etChangeRecordTypeNote),
                withText(note),
            ),
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.previewChangeRecordType), matcher))

    private fun openGoals() {
        clickOnView(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeGoalHint),
                withText(coreR.string.change_record_type_goal_time_hint),
            ),
        )
    }
}
