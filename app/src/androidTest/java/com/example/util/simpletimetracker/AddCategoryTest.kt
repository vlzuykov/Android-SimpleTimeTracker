package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddCategoryTest : BaseUiTest() {

    @Test
    fun addCategory() {
        val name = "Test"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val note = "note"
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1

        // Add activities
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        clickOnViewWithText(coreR.string.categories_add_category)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(changeCategoryR.id.btnChangeCategoryDelete))
        checkViewIsNotDisplayed(withId(changeCategoryR.id.btnChangeCategoryStatistics))
        checkViewIsNotDisplayed(withId(changeCategoryR.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(changeCategoryR.id.rvChangeCategoryType))

        // Name is not selected
        clickOnViewWithText(coreR.string.change_category_save)

        // Typing name
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsDisplayed(withId(changeCategoryR.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(changeCategoryR.id.rvChangeCategoryType))

        // Selecting color
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))
        checkViewIsDisplayed(
            allOf(withId(changeCategoryR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )

        // Selecting color
        scrollRecyclerToPosition(changeCategoryR.id.rvChangeCategoryColor, lastColorPosition)
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeCategoryR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )

        // Open activity chooser
        clickOnViewWithText(coreR.string.change_category_color_hint)
        clickOnViewWithText(coreR.string.change_category_types_hint)
        checkViewIsNotDisplayed(withId(changeCategoryR.id.rvChangeCategoryColor))
        checkViewIsDisplayed(withId(changeCategoryR.id.rvChangeCategoryType))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeCategoryR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(changeCategoryR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeCategoryR.id.viewDividerItem)))

        // Selecting activity
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName1))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(changeCategoryR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(changeCategoryR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeCategoryR.id.viewDividerItem)))

        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewDoesNotExist(withId(changeCategoryR.id.viewDividerItem))
        checkViewIsDisplayed(withText(typeName1))
        checkViewIsDisplayed(withText(typeName2))

        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName2))
        checkViewIsDisplayed(withText(coreR.string.nothing_selected))
        checkViewIsDisplayed(withId(changeCategoryR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(changeCategoryR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(changeCategoryR.id.viewDividerItem)))

        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryType, withText(typeName1))
        clickOnViewWithText(coreR.string.change_category_types_hint)

        // Selecting goal time
        openGoals()
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.layoutChangeRecordTypeGoalSession)),
                withId(R.id.fieldChangeRecordTypeGoalDuration),
            ),
        )
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("10$minuteString"))
        openGoals()

        // Adding note
        onView(withId(changeCategoryR.id.etChangeRecordCategoryNote)).perform(nestedScrollTo())
        typeTextIntoView(changeCategoryR.id.etChangeRecordCategoryNote, note)

        // Category added
        clickOnViewWithText(coreR.string.change_record_type_save)
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check types saved
        longClickOnView(withText(name))
        Thread.sleep(1000)
        clickOnViewWithText(coreR.string.change_category_types_hint)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(baseR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))
        clickOnViewWithText(coreR.string.change_category_types_hint)

        // Check goals saved
        Thread.sleep(1000)
        openGoals()
        checkViewIsDisplayed(withText("10$minuteString"))
        openGoals()

        // Check note saved
        onView(withId(changeCategoryR.id.etChangeRecordCategoryNote)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(changeCategoryR.id.etChangeRecordCategoryNote), withText(note)))
    }

    @Test
    fun addCategoryEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_category)

        // Goal time is disabled
        openGoals()
        checkViewIsDisplayed(
            allOf(
                isDescendantOfA(withId(R.id.layoutChangeRecordTypeGoalSession)),
                withId(R.id.tvChangeRecordTypeGoalDurationValue),
                withText(coreR.string.change_record_type_goal_time_disabled),
            ),
        )
        openGoals()

        // Open activity chooser
        clickOnViewWithText(coreR.string.change_category_types_hint)
        checkViewIsDisplayed(withText(coreR.string.record_types_empty))
    }

    @Test
    fun addCategoryFromChangeActivity() {
        val categoryName1 = "Category1"
        val categoryName2 = "Category2"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add activity
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)
        Thread.sleep(1000)
        tryAction { longClickOnView(withText(typeName1)) }

        // Add category
        Thread.sleep(1000)
        clickOnViewWithText(coreR.string.category_hint)
        clickOnViewWithText(coreR.string.categories_add_category)
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, categoryName1)
        closeSoftKeyboard()

        // Activity already selected
        clickOnViewWithText(coreR.string.change_category_types_hint)
        checkViewIsDisplayed(withText(coreR.string.something_selected))
        checkViewIsDisplayed(withId(baseR.id.viewDividerItem))
        onView(withText(typeName1)).check(isCompletelyAbove(withId(baseR.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(baseR.id.viewDividerItem)))

        // Category added
        clickOnViewWithText(coreR.string.change_category_save)
        checkViewIsDisplayed(withText(categoryName1))

        // Change category
        longClickOnView(withText(categoryName1))
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, categoryName2)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)

        // Category changed
        checkViewDoesNotExist(withText(categoryName1))
        checkViewIsDisplayed(withText(categoryName2))
    }

    @Test
    fun addCategorySameName() {
        val name = "Test"

        // Add activity
        testUtils.addCategory(name)

        // Add another
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_category)
        closeSoftKeyboard()

        // No error
        checkViewDoesNotExist(withText(coreR.string.change_record_message_name_exist))

        // Check same name
        typeTextIntoView(R.id.etChangeCategoryName, name)
        checkViewIsDisplayed(withText(coreR.string.change_record_message_name_exist))

        // Check other name
        typeTextIntoView(R.id.etChangeCategoryName, "$name+")
        checkViewDoesNotExist(withText(coreR.string.change_record_message_name_exist))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeCategoryR.id.previewChangeCategory), matcher))

    private fun openGoals() {
        clickOnView(
            allOf(
                withId(changeCategoryR.id.tvChangeCategoryGoalHint),
                withText(coreR.string.change_record_type_goal_time_hint),
            ),
        )
    }
}
