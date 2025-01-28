package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RepeatButtonType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import javax.inject.Inject

// Repeats previous record, if any.
class RecordRepeatInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun repeat(): ActionResult {
        return execute { messageResId ->
            SnackBarParams(
                message = resourceRepo.getString(messageResId),
                duration = SnackBarParams.Duration.Short,
            ).let(router::show)
        }
    }

    // Can be used than app is closed (ex. from widget).
    suspend fun repeatExternal() {
        execute { messageResId ->
            ToastParams(
                message = resourceRepo.getString(messageResId),
            ).let(router::show)
        }
    }

    suspend fun repeatWithoutMessage(): ActionResult {
        return execute(messageShower = {})
    }

    private suspend fun execute(
        messageShower: (messageResId: Int) -> Unit,
    ): ActionResult {
        val type = prefsInteractor.getRepeatButtonType()

        // TODO repeat several records?
        val prevRecord = recordInteractor.getPrev(
            timeStarted = System.currentTimeMillis(),
        ).let {
            when (type) {
                is RepeatButtonType.RepeatLast -> it
                is RepeatButtonType.RepeatBeforeLast -> if (it != null) {
                    recordInteractor.getPrev(timeStarted = it.timeEnded - 1)
                } else {
                    null
                }
            }
        } ?: run {
            messageShower(R.string.running_records_repeat_no_prev_record)
            return ActionResult.NoPreviousFound
        }
        if (runningRecordInteractor.get(prevRecord.typeId) != null) {
            messageShower(R.string.running_records_repeat_already_tracking)
            return ActionResult.AlreadyTracking
        }

        addRunningRecordMediator.startTimer(
            typeId = prevRecord.typeId,
            tagIds = prevRecord.tagIds,
            comment = prevRecord.comment,
        )
        return ActionResult.Started
    }

    sealed interface ActionResult {
        object Started : ActionResult
        object NoPreviousFound : ActionResult
        object AlreadyTracking : ActionResult
    }
}