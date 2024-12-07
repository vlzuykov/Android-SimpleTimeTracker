package com.example.util.simpletimetracker.feature_settings.mapper

import android.text.SpannableString
import android.text.SpannableStringBuilder
import com.example.util.simpletimetracker.core.extension.fromHtml
import com.example.util.simpletimetracker.core.manager.ClipboardManager
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_ADD_RECORD
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESTART_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_START_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_ALL_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STARTED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EVENT_STOPPED_ACTIVITY
import com.example.util.simpletimetracker.core.utils.EXTRA_ACTIVITY_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_COMMENT
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TAG_NAME
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_ICON
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TYPE_NOTE
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TIME_ENDED
import com.example.util.simpletimetracker.core.utils.EXTRA_RECORD_TIME_STARTED
import com.example.util.simpletimetracker.domain.extension.indexesOf
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_views.extension.setClickableSpan
import com.example.util.simpletimetracker.feature_views.extension.setImageSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.HelpDialogParams
import javax.inject.Inject

class SettingsAutomatedTrackingMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val router: Router,
    private val clipboardManager: ClipboardManager,
    private val applicationDataProvider: ApplicationDataProvider,
) {

    fun toAutomatedTrackingHelpDialog(
        isDarkTheme: Boolean,
    ): HelpDialogParams {
        val mainText = formatHelpText(
            string = R.string.settings_automated_tracking_text
                .let(resourceRepo::getString),
            isDarkTheme = isDarkTheme,
            arguments = listOf(
                HelpText(ACTION_EXTERNAL_START_ACTIVITY, canCopy = true),
                HelpText(ACTION_EXTERNAL_STOP_ACTIVITY, canCopy = true),
                HelpText(EXTRA_ACTIVITY_NAME, canCopy = true),
                HelpText(applicationDataProvider.getPackageName(), canCopy = true),
            ),
        )

        val sendEventsText = resourceRepo.getString(
            R.string.settings_automated_tracking_send_events_text,
            resourceRepo.getString(R.string.settings_automated_tracking_send_events),
        ).fromHtml()

        val availableActionsText = getAvailableActionsText(
            actions = listOf(
                AvailableAction(
                    action = ACTION_EXTERNAL_START_ACTIVITY,
                    extras = listOf(EXTRA_ACTIVITY_NAME),
                    optional = listOf(
                        EXTRA_RECORD_COMMENT,
                        EXTRA_RECORD_TAG_NAME,
                        EXTRA_RECORD_TIME_STARTED,
                    ),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_STOP_ACTIVITY,
                    extras = listOf(EXTRA_ACTIVITY_NAME),
                    optional = listOf(
                        EXTRA_RECORD_TIME_ENDED,
                    ),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_STOP_ALL_ACTIVITIES,
                    extras = emptyList(),
                    optional = emptyList(),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY,
                    extras = emptyList(),
                    optional = emptyList(),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY,
                    extras = emptyList(),
                    optional = emptyList(),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_RESTART_ACTIVITY,
                    extras = emptyList(),
                    optional = emptyList(),
                ),
                AvailableAction(
                    action = ACTION_EXTERNAL_ADD_RECORD,
                    extras = listOf(
                        EXTRA_ACTIVITY_NAME,
                        EXTRA_RECORD_TIME_STARTED,
                        EXTRA_RECORD_TIME_ENDED,
                    ),
                    optional = listOf(
                        EXTRA_RECORD_COMMENT,
                        EXTRA_RECORD_TAG_NAME,
                    ),
                ),
            ),
            isDarkTheme = isDarkTheme,
        )

        val availableEvents = getAvailableActionsText(
            actions = listOf(
                AvailableAction(
                    action = EVENT_STARTED_ACTIVITY,
                    extras = listOf(
                        EXTRA_ACTIVITY_NAME,
                        EXTRA_RECORD_COMMENT,
                        EXTRA_RECORD_TAG_NAME,
                        EXTRA_RECORD_TYPE_NOTE,
                        EXTRA_RECORD_TYPE_ICON,
                    ),
                    optional = emptyList(),
                ),
                AvailableAction(
                    action = EVENT_STOPPED_ACTIVITY,
                    extras = listOf(
                        EXTRA_ACTIVITY_NAME,
                        EXTRA_RECORD_COMMENT,
                        EXTRA_RECORD_TAG_NAME,
                        EXTRA_RECORD_TYPE_NOTE,
                        EXTRA_RECORD_TYPE_ICON,
                    ),
                    optional = emptyList(),
                ),
            ),
            isDarkTheme = isDarkTheme,
        )

        val extrasDescription = getExtrasDescriptions(
            extras = listOf(
                ExtraDescription(
                    extra = EXTRA_ACTIVITY_NAME,
                    description = resourceRepo.getString(R.string.settings_automated_tracking_extra_name),
                ),
                ExtraDescription(
                    extra = EXTRA_RECORD_TAG_NAME,
                    description = resourceRepo.getString(R.string.settings_automated_tracking_extra_tag),
                ),
                ExtraDescription(
                    extra = EXTRA_RECORD_TIME_STARTED,
                    description = resourceRepo.getString(R.string.settings_automated_tracking_extra_time),
                ),
                ExtraDescription(
                    extra = EXTRA_RECORD_TIME_ENDED,
                    description = resourceRepo.getString(R.string.settings_automated_tracking_extra_time),
                ),
            ),
        )

        val availableActionsHint = resourceRepo.getString(
            R.string.settings_automated_tracking_available_actions,
        ).uppercase()
        val availableEventsHint = resourceRepo.getString(
            R.string.settings_automated_tracking_available_events,
        ).uppercase()
        val extrasDescriptionsHint = resourceRepo.getString(
            R.string.settings_automated_tracking_extras_description,
        ).uppercase()

        val finalText = SpannableStringBuilder()
            .append(mainText).append("\n")
            .append(sendEventsText).append("\n")
            .append(availableActionsHint).append("\n\n")
            .append(availableActionsText)
            .append(availableEventsHint).append("\n\n")
            .append(availableEvents)
            .append(extrasDescriptionsHint).append("\n\n")
            .append(extrasDescription)

        return HelpDialogParams(
            title = resourceRepo.getString(R.string.settings_automated_tracking),
            text = finalText,
        )
    }

    // TODO add tests to check what runCatching not fails here and other places.
    private fun getAvailableActionsText(
        actions: List<AvailableAction>,
        isDarkTheme: Boolean,
    ): SpannableString = runCatching {
        val helpTexts = mutableListOf<HelpText>()
        actions.forEach {
            helpTexts += HelpText(it.action, canCopy = true)
            helpTexts += it.extras.map { HelpText(it, canCopy = true) }
            helpTexts += it.optional.map { HelpText(it, canCopy = true) }
        }

        val templateText = StringBuilder()
        actions.forEach {
            templateText.append(
                resourceRepo.getString(
                    R.string.settings_automated_tracking_data_template,
                    resourceRepo.getString(R.string.settings_automated_tracking_action),
                    "%s".wrapInQuotes(),
                ),
            )
            templateText.append("<br/>")
            if (it.extras.isNotEmpty()) {
                templateText.append(
                    resourceRepo.getString(
                        R.string.settings_automated_tracking_data_template,
                        resourceRepo.getString(R.string.settings_automated_tracking_extra),
                        it.extras.joinToString(separator = ", ") { "%s".wrapInQuotes() },
                    ),
                )
                templateText.append("<br/>")
            }
            if (it.optional.isNotEmpty()) {
                templateText.append(
                    resourceRepo.getString(
                        R.string.settings_automated_tracking_data_template,
                        resourceRepo.getString(R.string.settings_automated_tracking_optional),
                        it.optional.joinToString(separator = ", ") { "%s".wrapInQuotes() },
                    ),
                )
                templateText.append("<br/>")
            }
            templateText.append("<br/>")
        }

        return formatHelpText(
            string = resourceRepo.getString(
                R.string.settings_automated_tracking_template,
                templateText,
            ),
            isDarkTheme = isDarkTheme,
            arguments = helpTexts,
        )
    }.getOrNull() ?: SpannableString("")

    private fun getExtrasDescriptions(
        extras: List<ExtraDescription>,
    ): SpannableString = runCatching {
        val templateText = StringBuilder()
        extras.forEach {
            templateText.append(
                resourceRepo.getString(
                    R.string.settings_automated_tracking_data_template,
                    it.extra,
                    it.description,
                ),
            )
            templateText.append("<br/>")
            templateText.append("<br/>")
        }

        return resourceRepo.getString(
            R.string.settings_automated_tracking_template,
            templateText,
        ).fromHtml().toSpannableString()
    }.getOrNull() ?: SpannableString("")

    private fun formatHelpText(
        string: String,
        isDarkTheme: Boolean,
        arguments: List<HelpText>,
    ): SpannableString = runCatching {
        val imageTag = "IMAGE_TAG"
        val theme = if (isDarkTheme) R.style.AppThemeDark else R.style.AppTheme
        val copyableArguments = arguments.filter(HelpText::canCopy)

        fun insertImageTags(arguments: List<HelpText>): List<String> {
            return arguments.mapIndexed { index, arg ->
                if (arg.canCopy) {
                    "${arg.text} $imageTag"
                } else {
                    arg.text
                }
            }
        }

        fun insertClickableSpans(string: SpannableString): SpannableString {
            copyableArguments.map { it.text }.toSet().forEach { argument ->
                string.indexesOf(argument).forEach { index ->
                    string.setClickableSpan(
                        start = index,
                        // 1 is for space in between.
                        length = argument.length + imageTag.length + 1,
                        onClick = { copyToClipboard(argument) },
                    )
                }
            }
            return string
        }

        fun insertImageSpans(string: SpannableString): SpannableString {
            val icon = resourceRepo.getDrawable(R.drawable.action_copy)
                ?.mutate()
                ?.apply {
                    setTint(resourceRepo.getThemedAttr(R.attr.appTextHintColor, theme))
                }
                ?: return SpannableString("")

            string.indexesOf(imageTag).forEach { index ->
                string.setImageSpan(
                    start = index,
                    length = imageTag.length,
                    drawable = icon,
                    sizeDp = 16,
                )
            }
            return string
        }

        return insertImageTags(arguments)
            .let { string.format(*it.toTypedArray()) }
            .fromHtml()
            .toSpannableString()
            .let(::insertClickableSpans)
            .let(::insertImageSpans)
    }.getOrNull() ?: runCatching {
        // Fallback in case of spans error.
        string
            .format(*arguments.toTypedArray())
            .toSpannableString()
    }.getOrNull() ?: SpannableString("")

    private fun copyToClipboard(text: String) {
        clipboardManager.send(text)
        val message = resourceRepo.getString(R.string.copied_to_clipboard)
        SnackBarParams(
            message = "$message\n\n$text",
            duration = SnackBarParams.Duration.ExtraShort,
            inDialog = true,
        ).let(router::show)
    }

    private fun String.wrapInQuotes(): String {
        return "\"$this\""
    }

    private data class HelpText(
        val text: String,
        val canCopy: Boolean,
    )

    private data class AvailableAction(
        val action: String,
        val extras: List<String>,
        val optional: List<String>,
    )

    private data class ExtraDescription(
        val extra: String,
        val description: String,
    )
}