package com.example.util.simpletimetracker.feature_notification.goalTime.interactor

import android.graphics.Color
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.recordType.extension.getDailyCount
import com.example.util.simpletimetracker.domain.recordType.extension.getDailyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthlyCount
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthlyDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getSessionCount
import com.example.util.simpletimetracker.domain.recordType.extension.getSessionDuration
import com.example.util.simpletimetracker.domain.recordType.extension.getWeeklyCount
import com.example.util.simpletimetracker.domain.recordType.extension.getWeeklyDuration
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.goalTime.manager.NotificationGoalTimeParams
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class NotificationGoalParamsInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val timeMapper: TimeMapper,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
) {

    suspend fun execute(
        idData: RecordTypeGoal.IdData,
        range: RecordTypeGoal.Range,
        type: Type,
    ): NotificationGoalTimeParams? {
        val typeId = (idData as? RecordTypeGoal.IdData.Type)?.value
        val recordType = recordTypeInteractor.get(typeId.orZero())
        val categoryId = (idData as? RecordTypeGoal.IdData.Category)?.value
        val category = categoryInteractor.get(categoryId.orZero())
        if (recordType == null && category == null) return null
        val goals = when (idData) {
            is RecordTypeGoal.IdData.Type -> {
                recordTypeGoalInteractor.getByType(typeId.orZero())
            }
            is RecordTypeGoal.IdData.Category -> {
                recordTypeGoalInteractor.getByCategory(categoryId.orZero())
            }
        }
        val isDarkTheme = prefsInteractor.getDarkMode()

        val goal = when (type) {
            is Type.Duration -> {
                when (range) {
                    is RecordTypeGoal.Range.Session -> goals.getSessionDuration()
                    is RecordTypeGoal.Range.Daily -> goals.getDailyDuration()
                    is RecordTypeGoal.Range.Weekly -> goals.getWeeklyDuration()
                    is RecordTypeGoal.Range.Monthly -> goals.getMonthlyDuration()
                }
            }
            is Type.Count -> {
                when (range) {
                    is RecordTypeGoal.Range.Session -> goals.getSessionCount()
                    is RecordTypeGoal.Range.Daily -> goals.getDailyCount()
                    is RecordTypeGoal.Range.Weekly -> goals.getWeeklyCount()
                    is RecordTypeGoal.Range.Monthly -> goals.getMonthlyCount()
                }
            }
        }

        val goalValueString = when (type) {
            // ex. 5h 30m
            is Type.Duration -> {
                goal.value.let(timeMapper::formatDuration)
            }
            // ex. 3 Records
            is Type.Count -> {
                goal.value.let {
                    "$it " + resourceRepo.getQuantityString(
                        stringResId = R.plurals.statistics_detail_times_tracked,
                        quantity = it.toInt(),
                    )
                }
            }
        }

        val goalTypeString = when (range) {
            is RecordTypeGoal.Range.Session -> R.string.change_record_type_session_goal_time
            is RecordTypeGoal.Range.Daily -> R.string.change_record_type_daily_goal_time
            is RecordTypeGoal.Range.Weekly -> R.string.change_record_type_weekly_goal_time
            is RecordTypeGoal.Range.Monthly -> R.string.change_record_type_monthly_goal_time
        }.let(resourceRepo::getString).let { "($it)" }

        val subtype = goal?.subtype ?: RecordTypeGoal.Subtype.Goal

        val goalSubtypeString = when (subtype) {
            is RecordTypeGoal.Subtype.Goal -> R.string.notification_goal_time_description
            is RecordTypeGoal.Subtype.Limit -> R.string.notification_limit_time_description
        }.let(resourceRepo::getString)

        val description = goalSubtypeString +
            " - " +
            goalValueString +
            " " +
            goalTypeString

        val checkState = when (subtype) {
            is RecordTypeGoal.Subtype.Goal -> GoalCheckmarkView.CheckState.GOAL_REACHED
            is RecordTypeGoal.Subtype.Limit -> GoalCheckmarkView.CheckState.LIMIT_REACHED
        }

        return NotificationGoalTimeParams(
            idData = idData,
            goalRange = range,
            icon = recordType?.icon
                ?.let(iconMapper::mapIcon)
                ?: RecordTypeIcon.Text(""),
            color = (recordType?.color ?: category?.color)
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: Color.TRANSPARENT,
            text = recordType?.name ?: category?.name ?: "",
            description = description,
            checkState = checkState,
        )
    }

    sealed interface Type {
        data object Duration : Type
        data object Count : Type
    }
}