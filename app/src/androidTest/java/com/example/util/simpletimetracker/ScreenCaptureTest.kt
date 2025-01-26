package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ScreenCaptureTest : BaseUiTest() {

    @Test
    fun screenshots() {
        val colors = ColorMapper.getAvailableColors()
        val icons = iconImageMapper
            .getAvailableImages(loadSearchHints = false).values
            .flatten().associateBy { it.iconName }.mapValues { it.value.iconResId }
        val readType = "Read"
        val guitarType = "Guitar"
        val readTag = "Mody Dick"
        val readComment = "I think it's related to the whale"

        // Add data
        runBlocking {
            prefsInteractor.setCardOrder(CardOrder.COLOR)
            prefsInteractor.setNumberOfCards(5)
            prefsInteractor.setShowNotifications(true)
            prefsInteractor.setEnableRepeatButton(true)
            prefsInteractor.setStatisticsDetailRange(RangeLength.All)
            val pomodoroStarted = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(7)
            prefsInteractor.setPomodoroModeStartedTimestampMs(pomodoroStarted)
        }
        defaultTypes.forEach { type ->
            testUtils.addActivity(
                name = type.name,
                color = colors.getOrNull(type.colorId),
                icon = icons[type.icon],
                goals = if (type.goal != null) {
                    GoalsTestUtils.getDailyDurationGoal(type.goal * 60L).let(::listOf)
                } else {
                    emptyList()
                },
            )
        }
        testUtils.addRecordTag(tagName = readTag, typeName = readType)
        runBlocking {
            val filteredInStatistics = listOf("Work", "Sleep", "Commute")
            recordTypeRepo.getAll()
                .filter { it.name in filteredInStatistics }
                .map(RecordType::id)
                .let { prefsInteractor.setFilteredTypes(it) }
        }
        Thread.sleep(1000)

        // Records list from June 2 2020
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime - TimeUnit.DAYS.toMillis(2)
        }
        testUtils.addRecord("Read", calendar.getMillis(0, 0), calendar.getMillis(0, 20))
        testUtils.addRecord("Sleep", calendar.getMillis(0, 20), calendar.getMillis(8, 12))
        testUtils.addRecord("Breakfast", calendar.getMillis(8, 12), calendar.getMillis(8, 27))
        testUtils.addRecord("Work", calendar.getMillis(8, 27), calendar.getMillis(10, 28))
        testUtils.addRecord("Youtube", calendar.getMillis(10, 28), calendar.getMillis(10, 48))
        testUtils.addRecord("Work", calendar.getMillis(10, 48), calendar.getMillis(13, 0))
        testUtils.addRecord("Cooking", calendar.getMillis(13, 34), calendar.getMillis(13, 52))
        testUtils.addRecord("Youtube", calendar.getMillis(13, 34), calendar.getMillis(13, 42))
        testUtils.addRecord("Exercise", calendar.getMillis(13, 42), calendar.getMillis(13, 47))
        testUtils.addRecord("Youtube", calendar.getMillis(13, 47), calendar.getMillis(14, 32))
        testUtils.addRecord("Lunch", calendar.getMillis(13, 52), calendar.getMillis(14, 22))
        testUtils.addRecord("Work", calendar.getMillis(14, 32), calendar.getMillis(19, 33))
        testUtils.addRecord("Exercise", calendar.getMillis(19, 33), calendar.getMillis(19, 57))
        testUtils.addRecord("Language", calendar.getMillis(19, 36), calendar.getMillis(19, 57))
        testUtils.addRecord("Cooking", calendar.getMillis(19, 57), calendar.getMillis(20, 15))
        testUtils.addRecord("Youtube", calendar.getMillis(19, 57, 1), calendar.getMillis(23, 23))
        testUtils.addRecord("Dinner", calendar.getMillis(20, 15), calendar.getMillis(20, 39))
        testUtils.addRecord("Guitar", calendar.getMillis(20, 39), calendar.getMillis(22, 4))
        testUtils.addRecord("Meditate", calendar.getMillis(23, 23), calendar.getMillis(23, 29))
        testUtils.addRecord(
            typeName = "Read",
            timeStarted = calendar.getMillis(23, 38),
            timeEnded = calendar.getMillis(23, 59, 59) + TimeUnit.MINUTES.toMillis(8),
            tagNames = listOf(readTag),
            comment = readComment,
        )

        // For detailed statistics, 551 records total, duration 495h 25m.
        val durationsMinutes = listOf(60 + 24, 60 + 3, 60 + 25, 55, 49, 40, 42, 34, 46, 45, 45, 60 + 22)
        durationsMinutes.forEachIndexed { index, minutes ->
            if (index == 2) return@forEachIndexed // Already added.
            val timeStarted = calendar.apply {
                timeInMillis = currentTime
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                add(Calendar.DATE, -index)
            }.timeInMillis
            testUtils.addRecord(
                typeName = guitarType,
                timeStarted = timeStarted,
                timeEnded = timeStarted + TimeUnit.MINUTES.toMillis(minutes.toLong()),
            )
        }
        val timeEnded = calendar.apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            add(Calendar.DATE, -100)
        }.timeInMillis
        val additionalRecords = 550 - durationsMinutes.size
        val additionalRecordsDuration = TimeUnit.MINUTES.toMillis(30)
        repeat(additionalRecords) {
            testUtils.addRecord(guitarType, timeEnded - additionalRecordsDuration, timeEnded)
        }
        val desiredDuration = 495 * 60 + 25
        val additionalRecordDuration = desiredDuration -
            durationsMinutes.sum() -
            additionalRecords * 30
        val additionalRecordDurationMillis = TimeUnit.MINUTES.toMillis(additionalRecordDuration.toLong())
        testUtils.addRecord(guitarType, timeEnded - additionalRecordDurationMillis, timeEnded)

        // Main tab
        val hour = TimeUnit.HOURS.toMillis(1)
        testUtils.addRecord("Language", currentTime - hour, currentTime)
        testUtils.addRecord("Meditate", currentTime - hour, currentTime)
        tryAction { clickOnViewWithText(readType) }
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(readType)))
        clickOnViewWithText("-30")
        clickOnViewWithText("+1")
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(readTag))
        clickOnViewWithText(R.string.change_record_save)
    }

    companion object {
        private data class DefaultRecordType(
            val name: String,
            val icon: String,
            val colorId: Int,
            val goal: Int? = null, // Minutes
        )

        private val defaultTypes: List<DefaultRecordType> by lazy {
            listOf(
                DefaultRecordType(name = "Games", icon = "ic_headset_24px", colorId = 1),
                DefaultRecordType(name = "Tv", icon = "ic_desktop_windows_24px", colorId = 1),
                DefaultRecordType(name = "Youtube", icon = "ic_ondemand_video_24px", colorId = 1),

                DefaultRecordType(name = "Exercise", icon = "ic_fitness_center_24px", colorId = 3),
                DefaultRecordType(name = "Guitar", icon = "ic_audiotrack_24px", colorId = 3),
                DefaultRecordType(name = "Language", icon = "ic_chat_24px", colorId = 3, goal = 5),
                DefaultRecordType(name = "Meditate", icon = "ic_lightbulb_outline_24px", colorId = 3, goal = 5),
                DefaultRecordType(name = "Read", icon = "ic_import_contacts_24px", colorId = 3, goal = 30),

                DefaultRecordType(name = "Chores", icon = "ic_assignment_24px", colorId = 5),
                DefaultRecordType(name = "Cleaning", icon = "ic_delete_24px", colorId = 5),
                DefaultRecordType(name = "Indoors", icon = "ic_extension_24px", colorId = 5),
                DefaultRecordType(name = "Outdoors", icon = "ic_directions_walk_24px", colorId = 5),
                DefaultRecordType(name = "Shopping", icon = "ic_shopping_cart_24px", colorId = 5),

                DefaultRecordType(name = "Breakfast", icon = "ic_free_breakfast_24px", colorId = 7),
                DefaultRecordType(name = "Cooking", icon = "ic_restaurant_menu_24px", colorId = 7),
                DefaultRecordType(name = "Dinner", icon = "ic_local_bar_24px", colorId = 7),
                DefaultRecordType(name = "Lunch", icon = "ic_restaurant_24px", colorId = 7),

                DefaultRecordType(name = "Commute", icon = "ic_airport_shuttle_24px", colorId = 10),
                DefaultRecordType(name = "Sleep", icon = "ic_airline_seat_individual_suite_24px", colorId = 10),
                DefaultRecordType(name = "Work", icon = "ic_business_center_24px", colorId = 10, goal = 8 * 60),
            )
        }
    }
}
