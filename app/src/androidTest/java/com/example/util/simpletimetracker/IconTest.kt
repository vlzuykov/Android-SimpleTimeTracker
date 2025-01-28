package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.icon.IconEmojiType
import com.example.util.simpletimetracker.domain.icon.IconImageType
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nthChildOf
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.swipeUp
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_change_running_record.R as changeRunningRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class IconTest : BaseUiTest() {

    @Test
    fun iconEmojiTest() {
        val firstName = "first"
        val secondName = "last"

        // Add activity
        testUtils.addActivity(name = secondName, text = lastEmoji)

        // Open change record type screen
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Select emoji
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint),
            ),
        )
        tryAction { clickOnViewWithText(firstEmoji) }

        // Preview is updated
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.previewChangeRecordType), hasDescendant(withText(firstEmoji))),
        )

        // Save
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, firstName)
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type is created
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordTypeItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstEmoji)),
                ),
            )
        }

        // Start timer
        clickOnViewWithText(firstName)

        // Check running record
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(firstName)),
                hasDescendant(withText(firstEmoji)),
            ),
        )

        // Change running record
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(firstName)))
        checkViewIsDisplayed(
            allOf(withId(changeRunningRecordR.id.previewChangeRunningRecord), hasDescendant(withText(firstEmoji))),
        )
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(secondName))
        checkViewIsDisplayed(
            allOf(withId(changeRunningRecordR.id.previewChangeRunningRecord), hasDescendant(withText(lastEmoji))),
        )
        clickOnViewWithText(coreR.string.change_record_save)

        // Check running record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRunningRecordItem),
                    hasDescendant(withText(secondName)),
                    hasDescendant(withText(lastEmoji)),
                ),
            )
        }

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(secondName)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(secondName)),
                hasDescendant(withText(lastEmoji)),
                isCompletelyDisplayed(),
            ),
        )

        // Change record
        clickOnView(allOf(withText(secondName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), hasDescendant(withText(lastEmoji))))
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(firstName))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), hasDescendant(withText(firstEmoji))))
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstEmoji)),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }

    @Test
    fun iconTextTest() {
        val firstName = "firstName"
        val firstIconText = "firstIconText"
        val secondName = "secondName"
        val secondIconText = "secondIconText"

        // Add activity
        testUtils.addActivity(name = secondName, text = secondIconText)

        // Open change record type screen
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Select emoji
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_text_hint),
            ),
        )
        tryAction { typeTextIntoView(changeRecordTypeR.id.etIconSelectionText, firstIconText) }

        // Preview is updated
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.previewChangeRecordType), hasDescendant(withText(firstIconText))),
        )

        // Save
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeIcon)
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, firstName)
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type is created
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordTypeItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstIconText)),
                ),
            )
        }

        // Start timer
        clickOnViewWithText(firstName)

        // Check running record
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(firstName)),
                hasDescendant(withText(firstIconText)),
            ),
        )

        // Change running record
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(firstName)))
        checkViewIsDisplayed(
            allOf(withId(changeRunningRecordR.id.previewChangeRunningRecord), hasDescendant(withText(firstIconText))),
        )
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(secondName))
        checkViewIsDisplayed(
            allOf(withId(changeRunningRecordR.id.previewChangeRunningRecord), hasDescendant(withText(secondIconText))),
        )
        clickOnViewWithText(coreR.string.change_record_save)

        // Check running record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRunningRecordItem),
                    hasDescendant(withText(secondName)),
                    hasDescendant(withText(secondIconText)),
                ),
            )
        }

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(secondName)))

        // Record is added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                hasDescendant(withText(secondName)),
                hasDescendant(withText(secondIconText)),
                isCompletelyDisplayed(),
            ),
        )

        // Change record
        clickOnView(allOf(withText(secondName), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.previewChangeRecord), hasDescendant(withText(secondIconText))),
        )
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(firstName))
        checkViewIsDisplayed(
            allOf(withId(changeRecordR.id.previewChangeRecord), hasDescendant(withText(firstIconText))),
        )
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check record
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(baseR.id.viewRecordItem),
                    hasDescendant(withText(firstName)),
                    hasDescendant(withText(firstIconText)),
                    isCompletelyDisplayed(),
                ),
            )
        }
    }

    @Test
    fun iconImageCategorySelection() {
        // Open record type add
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Open image icons
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)

        // Check categories
        iconImageMapper.getAvailableImages(loadSearchHints = false).forEach { (category, images) ->
            if (category.type == IconImageType.FAVOURITES) return@forEach

            checkViewIsDisplayed(withTag(category.categoryIcon))
            clickOnView(withTag(category.categoryIcon))
            val firstImage = images.first().iconResId

            if (category == iconImageMapper.getAvailableCategories(hasFavourites = false).last()) {
                onView(
                    withId(changeRecordTypeR.id.rvIconSelection),
                ).perform(collapseToolbar())
                onView(
                    withId(changeRecordTypeR.id.rvIconSelection),
                ).perform(swipeUp(50))
            }

            // Check category hint
            checkViewIsDisplayed(withText(category.name))
            // Check first icon in category
            checkViewIsDisplayed(withTag(firstImage))
        }
    }

    @Test
    fun iconEmojiCategorySelection() {
        // Open record type add
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Open emoji icons
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint),
            ),
        )

        // Check categories
        iconEmojiMapper.getAvailableEmojis(loadSearchHints = false).forEach { (category, emojis) ->
            if (category.type == IconEmojiType.FAVOURITES) return@forEach

            checkViewIsDisplayed(withTag(category.categoryIcon))
            clickOnView(withTag(category.categoryIcon))
            val firstEmoji = iconEmojiMapper.toEmojiString(emojis.first().emojiCode)

            // Check category hint
            checkViewIsDisplayed(withText(category.name))
            // Check first icon in category
            checkViewIsDisplayed(withText(firstEmoji))
        }
    }

    @Test
    fun skinToneSelectionDialog() {
        val name = "name"
        val category = iconEmojiMapper.getAvailableEmojiCategories(hasFavourites = false)
            .first { it.type == IconEmojiType.PEOPLE }
        val emoji = iconEmojiMapper.getAvailableEmojis(loadSearchHints = false)[category]
            ?.first()?.emojiCode
            ?: throw RuntimeException()
        val emojiDefault = emoji.let(iconEmojiMapper::toEmojiString)
        val emojiSkinTones = emoji.let(iconEmojiMapper::toSkinToneVariations)
        val emojiSkinTone = emojiSkinTones.last()

        // Open record type add
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)

        // Open emoji icons
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint),
            ),
        )
        onView(withId(changeRecordTypeR.id.rvIconSelection)).perform(collapseToolbar())
        Thread.sleep(1000)
        scrollRecyclerToView(changeRecordTypeR.id.rvIconSelection, hasDescendant(withText(emojiDefault)))
        clickOnRecyclerItem(changeRecordTypeR.id.rvIconSelection, withText(emojiDefault))

        // Check dialog
        onView(withId(dialogsR.id.rvEmojiSelectionContainer)).check(recyclerItemCount(6))

        // Check emojis
        checkViewIsDisplayed(withText(emojiDefault))
        emojiSkinTones.forEach {
            checkViewIsDisplayed(withText(it))
        }

        clickOnViewWithText(emojiSkinTone)

        // Preview is updated
        checkViewIsDisplayed(
            allOf(withId(changeRecordTypeR.id.previewChangeRecordType), hasDescendant(withText(emojiSkinTone))),
        )

        // Check record type
        clickOnViewWithText(coreR.string.change_record_type_save)
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(name)),
                hasDescendant(withText(emojiSkinTone)),
            ),
        )
    }

    @Test
    fun favouriteIcons() {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Check icons
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)

        // Check first category
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.imageGroupMaps),
            ),
        )

        // Add to favourites
        val (category, images) = iconImageMapper.getAvailableImages(loadSearchHints = false)
            .toList().dropLast(1).last()
        clickOnView(withTag(category.categoryIcon))
        val firstImage = images.first().iconResId
        checkViewIsDisplayed(withText(category.name))
        clickOnView(withTag(firstImage))
        clickOnViewWithId(R.id.btnIconSelectionFavourite)
        clickOnView(withTag(R.drawable.icon_category_image_favourite))
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.change_record_favourite_comments_hint),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 1),
                hasDescendant(withTag(firstImage)),
            ),
        )

        // Remove from favourites
        clickOnViewWithId(R.id.btnIconSelectionFavourite)
        checkViewDoesNotExist(withTag(R.drawable.icon_category_image_favourite))
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.imageGroupMaps),
            ),
        )

        // Check emojis
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTypeR.id.btnIconSelectionSwitch)),
                withText(coreR.string.change_record_type_icon_emoji_hint),
            ),
        )

        // Check first category
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.emojiGroupSmileys),
            ),
        )

        // Add to favourites
        val (emojiCategory, emojis) = iconEmojiMapper.getAvailableEmojis(loadSearchHints = false)
            .toList().last()
        clickOnView(withTag(emojiCategory.categoryIcon))
        val firstEmoji = emojis.first().emojiCode
        checkViewIsDisplayed(withText(emojiCategory.name))
        clickOnView(withText(firstEmoji))
        clickOnViewWithId(R.id.btnIconSelectionFavourite)
        clickOnView(withTag(R.drawable.icon_category_image_favourite))
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.change_record_favourite_comments_hint),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 1),
                hasDescendant(withText(firstEmoji)),
            ),
        )

        // Remove from favourites
        clickOnViewWithId(R.id.btnIconSelectionFavourite)
        checkViewDoesNotExist(withTag(R.drawable.icon_category_image_favourite))
        checkViewIsDisplayed(
            allOf(
                nthChildOf(withId(R.id.rvIconSelection), 0),
                withText(R.string.emojiGroupSmileys),
            ),
        )
    }
}
