package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsBlockHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonBigViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsButtonViewData
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.adapter.RecordQuickActionsWidthHolder
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsButton
import com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model.RecordQuickActionsState
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordQuickActionsParams.Type
import javax.inject.Inject

class RecordQuickActionsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
) {

    suspend fun getViewData(
        extra: RecordQuickActionsParams,
    ): RecordQuickActionsState {
        val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
        val canContinue = !retroactiveTrackingModeEnabled
        val allowedButtons = getAllowedButtons(extra, canContinue)
        val buttons = getAllButtons().filter {
            val block = (it as? RecordQuickActionsBlockHolder)?.block
            block in allowedButtons
        }.let(::applyWidth)

        return RecordQuickActionsState(
            buttons = buttons,
        )
    }

    private fun getAllButtons(): List<ViewHolderType> {
        return listOf(
            RecordQuickActionsButtonBigViewData(
                block = RecordQuickActionsButton.STATISTICS,
                text = R.string.shortcut_navigation_statistics.let(resourceRepo::getString),
                icon = R.drawable.statistics,
            ),
            RecordQuickActionsButtonBigViewData(
                block = RecordQuickActionsButton.DELETE,
                text = R.string.archive_dialog_delete.let(resourceRepo::getString),
                icon = R.drawable.delete,
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.CONTINUE,
                text = R.string.change_record_continue.let(resourceRepo::getString),
                icon = R.drawable.action_continue,
                iconColor = resourceRepo.getColor(R.color.red_300),
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.REPEAT,
                text = R.string.change_record_repeat.let(resourceRepo::getString),
                icon = R.drawable.repeat,
                iconColor = resourceRepo.getColor(R.color.purple_300),
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.DUPLICATE,
                text = R.string.change_record_duplicate.let(resourceRepo::getString),
                icon = R.drawable.action_copy,
                iconColor = resourceRepo.getColor(R.color.indigo_300),
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.MERGE,
                text = R.string.change_record_merge.let(resourceRepo::getString),
                icon = R.drawable.action_merge,
                iconColor = resourceRepo.getColor(R.color.light_blue_300),
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.STOP,
                text = R.string.notification_record_type_stop.let(resourceRepo::getString),
                icon = R.drawable.action_stop,
                iconColor = resourceRepo.getColor(R.color.teal_300),
            ),
            RecordQuickActionsButtonViewData(
                block = RecordQuickActionsButton.CHANGE_ACTIVITY,
                text = resourceRepo.getString(R.string.data_edit_change_activity),
                icon = R.drawable.action_change_item,
                iconColor = resourceRepo.getColor(R.color.green_300),
            ),
        )
    }

    private fun getAllowedButtons(
        extra: RecordQuickActionsParams,
        canContinue: Boolean,
    ): List<RecordQuickActionsButton> {
        return when (extra.type) {
            is Type.RecordTracked -> listOfNotNull(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.CONTINUE.takeIf { canContinue },
                RecordQuickActionsButton.REPEAT,
                RecordQuickActionsButton.DUPLICATE,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
            )
            is Type.RecordUntracked -> listOf(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.MERGE,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
            )
            is Type.RecordRunning -> listOf(
                RecordQuickActionsButton.STATISTICS,
                RecordQuickActionsButton.DELETE,
                RecordQuickActionsButton.STOP,
                RecordQuickActionsButton.CHANGE_ACTIVITY,
            )
            null -> emptyList()
        }
    }

    private fun applyWidth(
        buttons: List<ViewHolderType>,
    ): List<ViewHolderType> {
        val bigButtonsCount = buttons
            .count { it is RecordQuickActionsButtonBigViewData }
        val bigButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonBigViewData }
        val smallButtonsCount = buttons
            .count { it is RecordQuickActionsButtonViewData }
        val smallButtonLastIndex = buttons
            .indexOfLast { it is RecordQuickActionsButtonViewData }

        return buttons.mapIndexed { index, button ->
            when {
                button is RecordQuickActionsButtonBigViewData &&
                    bigButtonsCount % 2 != 0 && index == bigButtonLastIndex -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                button is RecordQuickActionsButtonViewData &&
                    smallButtonsCount % 2 != 0 && index == smallButtonLastIndex -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                else -> button
            }
        }
    }
}