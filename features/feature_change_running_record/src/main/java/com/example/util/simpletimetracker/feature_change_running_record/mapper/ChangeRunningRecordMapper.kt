package com.example.util.simpletimetracker.feature_change_running_record.mapper

import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordFromChangeScreenInteractor
import com.example.util.simpletimetracker.domain.record.interactor.UpdateRunningRecordFromChangeScreenInteractor.GoalState
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData.Subtype
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import javax.inject.Inject

class ChangeRunningRecordMapper @Inject constructor() {

    fun map(
        fullUpdate: Boolean,
        recordPreview: RunningRecordViewData,
    ): UpdateRunningRecordFromChangeScreenInteractor.Update {
        return UpdateRunningRecordFromChangeScreenInteractor.Update(
            id = recordPreview.id,
            timer = recordPreview.timer,
            timerTotal = recordPreview.timerTotal,
            goalText = recordPreview.goalTime.text,
            goalState = when (recordPreview.goalTime.state) {
                is Subtype.Hidden -> GoalState.Hidden
                is Subtype.Goal -> GoalState.Goal
                is Subtype.Limit -> GoalState.Limit
            },
            additionalData = if (fullUpdate) {
                UpdateRunningRecordFromChangeScreenInteractor.AdditionalData(
                    tagName = recordPreview.tagName,
                    timeStarted = recordPreview.timeStarted,
                    comment = recordPreview.comment,
                )
            } else {
                null
            },
        )
    }
}