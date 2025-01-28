package com.example.util.simpletimetracker.feature_data_edit.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.recordTag.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRecordsCountState
import javax.inject.Inject

class DateEditViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val recordFilterInteractor: RecordFilterInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
) {

    suspend fun getSelectedRecordsCount(
        filters: List<RecordsFilter>,
    ): DataEditRecordsCountState {
        val records = recordFilterInteractor.getByFilter(filters)
            .filterIsInstance<Record>()
        val selectedRecordsCount = records.size
        val recordsString = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            selectedRecordsCount,
        ).lowercase()

        return DataEditRecordsCountState(
            count = selectedRecordsCount,
            countText = "$selectedRecordsCount $recordsString",
        )
    }

    suspend fun getChangeActivityState(
        newTypeId: Long,
    ): DataEditChangeActivityState {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = newTypeId.let { recordTypeInteractor.get(it) }

        return if (type == null) {
            DataEditChangeActivityState.Disabled
        } else {
            DataEditChangeActivityState.Enabled(
                recordTypeViewDataMapper.map(
                    recordType = type,
                    isDarkTheme = isDarkTheme,
                ),
            )
        }
    }

    fun getChangeCommentState(
        newComment: String,
    ): DataEditChangeCommentState {
        return DataEditChangeCommentState.Enabled(newComment)
    }

    suspend fun getTagState(
        tagIds: List<Long>,
    ): List<CategoryViewData.Record> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll().associateBy { it.id }

        return recordTagInteractor.getAll()
            .filter { it.id in tagIds }
            .map {
                categoryViewDataMapper.mapRecordTag(
                    tag = it,
                    type = types[it.iconColorSource],
                    isDarkTheme = isDarkTheme,
                )
            }
    }

    suspend fun getChangeButtonState(
        enabled: Boolean,
    ): DataEditChangeButtonState {
        val theme = if (prefsInteractor.getDarkMode()) {
            R.style.AppThemeDark
        } else {
            R.style.AppTheme
        }

        return DataEditChangeButtonState(
            enabled = enabled,
            backgroundTint = (if (enabled) R.attr.appActiveColor else R.attr.appInactiveColor)
                .let { resourceRepo.getThemedAttr(it, theme) },
        )
    }

    fun filterTags(
        typeForTagSelection: Long?,
        tags: List<CategoryViewData.Record>,
        typesToTags: List<RecordTypeToTag>,
    ): List<CategoryViewData.Record> {
        val typeId = typeForTagSelection.orZero()

        val selectableTagIds = filterSelectableTagsInteractor.execute(
            tagIds = tags.map { it.id },
            typesToTags = typesToTags,
            typeIds = listOf(typeId),
        )

        return tags.filter { it.id in selectableTagIds }
    }
}