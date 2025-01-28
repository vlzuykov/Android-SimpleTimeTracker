/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.GetCurrentActivitySuggestionsInteractor
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.statistics.interactor.SettingsDataUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.ShouldShowRecordDataSelectionInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordDataSelectionDialogResult
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.widget.model.WidgetType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.wear_api.WearActivityDTO
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearCurrentStateDTO
import com.example.util.simpletimetracker.wear_api.WearRecordRepeatResponse
import com.example.util.simpletimetracker.wear_api.WearSettingsDTO
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionRequest
import com.example.util.simpletimetracker.wear_api.WearShouldShowTagSelectionResponse
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import com.example.util.simpletimetracker.wear_api.WearTagDTO
import dagger.Lazy
import javax.inject.Inject

class WearDataRepo @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val shouldShowRecordDataSelectionInteractor: ShouldShowRecordDataSelectionInteractor,
    private val removeRunningRecordMediator: Lazy<RemoveRunningRecordMediator>,
    private val addRunningRecordMediator: Lazy<AddRunningRecordMediator>,
    private val recordRepeatInteractor: Lazy<RecordRepeatInteractor>,
    private val updateExternalViewsInteractor: Lazy<UpdateExternalViewsInteractor>,
    private val router: Router,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDataUpdateInteractor: SettingsDataUpdateInteractor,
    private val wearDataLocalMapper: WearDataLocalMapper,
    private val getCurrentActivitySuggestionsInteractor: GetCurrentActivitySuggestionsInteractor,
) : WearCommunicationAPI {

    override suspend fun queryActivities(): List<WearActivityDTO> {
        return recordTypeInteractor.getAll()
            .filter { recordType -> !recordType.hidden }
            .map(wearDataLocalMapper::map)
    }

    override suspend fun queryCurrentActivities(): WearCurrentStateDTO {
        val tags = recordTagInteractor.getAll().associateBy(RecordTag::id)

        fun mapTags(tagIds: List<Long>): List<WearTagDTO> {
            return tagIds.mapNotNull { tagId ->
                wearDataLocalMapper.map(
                    recordTag = tags[tagId] ?: return@mapNotNull null,
                    types = emptyMap(), // Color is not needed.
                )
            }
        }

        val recordTypesMapProvider: suspend () -> Map<Long, RecordType> = {
            recordTypeInteractor.getAll().associateBy(RecordType::id)
        }
        val runningRecords = runningRecordInteractor.getAll()
        val runningRecordsData = runningRecords.map { record ->
            wearDataLocalMapper.map(record, mapTags(record.tagIds))
        }
        val prevRecordsData = recordInteractor
            .getAllPrev(timeStarted = System.currentTimeMillis())
            .map { record ->
                wearDataLocalMapper.map(record, mapTags(record.tagIds))
            }
        val suggestionsData = getCurrentActivitySuggestionsInteractor.execute(
            recordTypesMapProvider = recordTypesMapProvider,
            runningRecords = runningRecords,
        ).map(RecordType::id)

        return WearCurrentStateDTO(
            currentActivities = runningRecordsData,
            lastRecords = prevRecordsData,
            suggestionIds = suggestionsData,
        )
    }

    override suspend fun startActivity(request: WearStartActivityRequest) {
        addRunningRecordMediator.get().startTimer(
            typeId = request.id,
            tagIds = request.tagIds,
            comment = "",
        )
        if (recordTypeInteractor.get(request.id)?.defaultDuration.orZero() > 0) {
            updateExternalViewsInteractor.get().onInstantRecordAdd()
        }
    }

    override suspend fun stopActivity(request: WearStopActivityRequest) {
        val current = runningRecordInteractor.get(request.id) ?: return
        removeRunningRecordMediator.get().removeWithRecordAdd(current)
    }

    override suspend fun repeatActivity(): WearRecordRepeatResponse {
        return recordRepeatInteractor.get().repeatWithoutMessage()
            .let(wearDataLocalMapper::map)
    }

    override suspend fun queryTagsForActivity(activityId: Long): List<WearTagDTO> {
        val types = recordTypeInteractor.getAll().associateBy { it.id }
        return getSelectableTagsInteractor.execute(activityId)
            .filterNot { it.archived }
            .map {
                wearDataLocalMapper.map(
                    recordTag = it,
                    types = types,
                )
            }
    }

    override suspend fun queryShouldShowTagSelection(
        request: WearShouldShowTagSelectionRequest,
    ): WearShouldShowTagSelectionResponse {
        val result = shouldShowRecordDataSelectionInteractor.execute(
            typeId = request.id,
            commentInputAvailable = false,
        )
        return WearShouldShowTagSelectionResponse(
            shouldShow = RecordDataSelectionDialogResult.Field.Tags in result.fields,
        )
    }

    override suspend fun querySettings(): WearSettingsDTO {
        return wearDataLocalMapper.map(
            allowMultitasking = prefsInteractor.getAllowMultitasking(),
            recordTagSelectionCloseAfterOne = prefsInteractor.getRecordTagSelectionCloseAfterOne(),
            enableRepeatButton = prefsInteractor.getEnableRepeatButton(),
            retroactiveTrackingMode = prefsInteractor.getRetroactiveTrackingMode(),
        )
    }

    override suspend fun setSettings(settings: WearSettingsDTO) {
        prefsInteractor.setAllowMultitasking(settings.allowMultitasking)
        widgetInteractor.updateWidgets(WidgetType.QUICK_SETTINGS)
        settingsDataUpdateInteractor.send()
    }

    override suspend fun openPhoneApp() {
        router.startApp()
    }
}