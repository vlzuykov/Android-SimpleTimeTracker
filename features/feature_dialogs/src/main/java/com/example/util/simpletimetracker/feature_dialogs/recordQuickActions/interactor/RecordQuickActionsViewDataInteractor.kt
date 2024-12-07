package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.interactor

import com.example.util.simpletimetracker.core.mapper.RecordQuickActionMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.RecordQuickAction
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
    private val recordQuickActionMapper: RecordQuickActionMapper,
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
        val allActions = listOf(
            RecordQuickActionsButton.CONTINUE,
            RecordQuickActionsButton.REPEAT,
            RecordQuickActionsButton.DUPLICATE,
            RecordQuickActionsButton.MERGE,
            RecordQuickActionsButton.STOP,
            RecordQuickActionsButton.CHANGE_ACTIVITY,
        )
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
        ) + allActions.mapNotNull {
            val action = mapAction(it) ?: return@mapNotNull null
            RecordQuickActionsButtonViewData(
                block = it,
                text = recordQuickActionMapper.mapText(action),
                icon = recordQuickActionMapper.mapIcon(action),
                iconColor = recordQuickActionMapper.mapColor(action),
            )
        }
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
            val isBigButtonFullWidth = bigButtonsCount % 2 != 0 && index == bigButtonLastIndex
            val isSmallButtonFullWidth = smallButtonsCount % 2 != 0 && index == smallButtonLastIndex
            when {
                button is RecordQuickActionsButtonBigViewData && isBigButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                button is RecordQuickActionsButtonViewData && isSmallButtonFullWidth -> {
                    button.copy(width = RecordQuickActionsWidthHolder.Width.Full)
                }
                else -> button
            }
        }
    }

    private fun mapAction(
        action: RecordQuickActionsButton,
    ): RecordQuickAction? {
        return when (action) {
            RecordQuickActionsButton.STATISTICS -> null
            RecordQuickActionsButton.DELETE -> null
            RecordQuickActionsButton.CONTINUE -> RecordQuickAction.CONTINUE
            RecordQuickActionsButton.REPEAT -> RecordQuickAction.REPEAT
            RecordQuickActionsButton.DUPLICATE -> RecordQuickAction.DUPLICATE
            RecordQuickActionsButton.MERGE -> RecordQuickAction.MERGE
            RecordQuickActionsButton.STOP -> RecordQuickAction.STOP
            RecordQuickActionsButton.CHANGE_ACTIVITY -> RecordQuickAction.CHANGE_ACTIVITY
        }
    }
}