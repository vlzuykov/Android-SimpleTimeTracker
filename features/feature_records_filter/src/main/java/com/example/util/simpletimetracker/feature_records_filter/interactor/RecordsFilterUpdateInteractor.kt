package com.example.util.simpletimetracker.feature_records_filter.interactor

import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.domain.base.UNCATEGORIZED_ITEM_ID
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.record.extension.getAllTypeIds
import com.example.util.simpletimetracker.domain.record.extension.getCategoryItems
import com.example.util.simpletimetracker.domain.record.extension.getCommentItems
import com.example.util.simpletimetracker.domain.record.extension.getDaysOfWeek
import com.example.util.simpletimetracker.domain.record.extension.getDuplicationItems
import com.example.util.simpletimetracker.domain.record.extension.getFilteredTags
import com.example.util.simpletimetracker.domain.record.extension.getManuallyFilteredRecordIds
import com.example.util.simpletimetracker.domain.record.extension.getSelectedTags
import com.example.util.simpletimetracker.domain.record.extension.getTypeIds
import com.example.util.simpletimetracker.domain.record.extension.getTypeIdsFromCategories
import com.example.util.simpletimetracker.domain.record.extension.hasMultitaskFilter
import com.example.util.simpletimetracker.domain.record.extension.hasUntrackedFilter
import com.example.util.simpletimetracker.domain.record.interactor.GetDuplicatedRecordsInteractor
import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordTag.interactor.FilterSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import com.example.util.simpletimetracker.feature_records_filter.mapper.RecordsFilterViewDataMapper
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterCommentType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterDateType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterDuplicationsType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordFilterType
import com.example.util.simpletimetracker.feature_records_filter.model.RecordsFilterSelectedRecordsViewData
import com.example.util.simpletimetracker.feature_records_filter.viewData.RecordsFilterSelectionButtonType
import javax.inject.Inject

class RecordsFilterUpdateInteractor @Inject constructor(
    private val filterSelectableTagsInteractor: FilterSelectableTagsInteractor,
    private val recordsFilterViewDataMapper: RecordsFilterViewDataMapper,
    private val getDuplicatedRecordsInteractor: GetDuplicatedRecordsInteractor,
    private val recordFilterInteractor: RecordFilterInteractor,
) {

    fun handleTypeClick(
        id: Long,
        currentFilters: List<RecordsFilter>,
        recordTypes: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentIds = filters.getTypeIds().toMutableList()
        val currentIdsFromCategories = filters.getTypeIdsFromCategories(
            recordTypes = recordTypes,
            recordTypeCategories = recordTypeCategories,
        )

        // Switch from categories to types in these categories.
        if (currentIdsFromCategories.isNotEmpty()) {
            currentIds.addAll(currentIdsFromCategories)
        }

        val newIds = currentIds.toMutableList().apply { addOrRemove(id) }

        return handleSelectTypes(filters, newIds)
    }

    fun handleCategoryClick(
        id: Long,
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentItems = filters.getCategoryItems()

        val newItems = if (id == UNCATEGORIZED_ITEM_ID) {
            RecordsFilter.CategoryItem.Uncategorized
        } else {
            RecordsFilter.CategoryItem.Categorized(id)
        }.let { currentItems.toMutableList().apply { addOrRemove(it) } }

        return handleSelectCategories(filters, newItems)
    }

    fun handleTagClick(
        currentState: RecordFilterType,
        currentFilters: List<RecordsFilter>,
        item: CategoryViewData.Record,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentTags = when (currentState) {
            RecordFilterType.SelectedTags -> filters.getSelectedTags()
            RecordFilterType.FilteredTags -> filters.getFilteredTags()
            else -> return currentFilters
        }

        val newTags = when (item) {
            is CategoryViewData.Record.Tagged -> RecordsFilter.TagItem.Tagged(item.id)
            is CategoryViewData.Record.Untagged -> RecordsFilter.TagItem.Untagged
        }.let { currentTags.toMutableList().apply { addOrRemove(it) } }

        return handleSelectTags(currentState, filters, newTags)
    }

    fun handleUntrackedClick(
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val hasUntrackedFilter = filters.hasUntrackedFilter()

        if (!hasUntrackedFilter) {
            val filtersAvailableWithUntrackedFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithUntrackedFilter
            }

            filters.add(RecordsFilter.Untracked)
        } else {
            filters.removeAll { it is RecordsFilter.Untracked }
        }

        return filters
    }

    fun handleMultitaskClick(
        currentFilters: List<RecordsFilter>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val hasMultitaskFilter = filters.hasMultitaskFilter()

        if (!hasMultitaskFilter) {
            val filtersAvailableWithMultitaskFilter = listOf(
                RecordsFilter.Date::class.java,
                RecordsFilter.DaysOfWeek::class.java,
                RecordsFilter.TimeOfDay::class.java,
                RecordsFilter.Duration::class.java,
            )
            filters.removeAll {
                it::class.java !in filtersAvailableWithMultitaskFilter
            }

            filters.add(RecordsFilter.Multitask)
        } else {
            filters.removeAll { it is RecordsFilter.Multitask }
        }

        return filters
    }

    fun handleDuplicationsFilterClick(
        currentFilters: List<RecordsFilter>,
        itemType: FilterViewData.Type,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentItems = filters.getDuplicationItems()

        val clickedItem = when (itemType) {
            is RecordFilterDuplicationsType.SameActivity -> {
                RecordsFilter.DuplicationsItem.SameActivity
            }
            is RecordFilterDuplicationsType.SameTimes -> {
                RecordsFilter.DuplicationsItem.SameTimes
            }
            else -> return currentFilters
        }
        val hasDefaultItem = currentItems.any { it is RecordsFilter.DuplicationsItem.SameTimes }
        val defaultItemIsClicked = clickedItem is RecordsFilter.DuplicationsItem.SameTimes
        val newItems = currentItems.toMutableList().apply {
            when {
                hasDefaultItem && defaultItemIsClicked -> {
                    // Remove all filters if default will be removed.
                    clear()
                }
                !hasDefaultItem && !defaultItemIsClicked -> {
                    // Add default filter if it is not added.
                    add(RecordsFilter.DuplicationsItem.SameTimes)
                    addOrRemove(clickedItem)
                }
                else -> {
                    addOrRemove(clickedItem)
                }
            }
        }

        filters.removeAll { it is RecordsFilter.Duplications }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Duplications(newItems))

        return filters
    }

    fun handleCommentFilterClick(
        currentFilters: List<RecordsFilter>,
        itemType: FilterViewData.Type,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val currentItems = filters.getCommentItems()

        val clickedItem = when (itemType) {
            is RecordFilterCommentType.NoComment -> {
                RecordsFilter.CommentItem.NoComment
            }
            is RecordFilterCommentType.AnyComment -> {
                RecordsFilter.CommentItem.AnyComment
            }
            else -> return currentFilters
        }
        val newItems = currentItems.toMutableList().apply {
            if (clickedItem !in this) clear()
            addOrRemove(clickedItem)
        }

        filters.removeAll { it is RecordsFilter.Comment }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Comment(newItems))

        return filters
    }

    fun handleCommentChange(
        currentFilters: List<RecordsFilter>,
        text: String,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Comment }
        if (text.isNotEmpty()) {
            val newItems = RecordsFilter.CommentItem.Comment(text).let(::listOf)
            filters.add(RecordsFilter.Comment(newItems))
        }
        return filters
    }

    fun handleRecordClick(
        currentFilters: List<RecordsFilter>,
        id: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val newIds = filters.getManuallyFilteredRecordIds()
            .toMutableMap()
            .apply { addOrRemove(id, true) }
        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(newIds.keys.toList()))
        return filters
    }

    fun handleInvertSelection(
        currentFilters: List<RecordsFilter>,
        recordsViewData: RecordsFilterSelectedRecordsViewData?,
    ): List<RecordsFilter> {
        if (recordsViewData == null || recordsViewData.isLoading) return currentFilters

        val filters = currentFilters.toMutableList()
        val filteredIds = filters.getManuallyFilteredRecordIds()
        val selectedIds = recordsViewData
            .recordsViewData
            .filterIsInstance<RecordViewData.Tracked>()
            .filter { it.id !in filteredIds }
            .map { it.id }

        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        if (selectedIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(selectedIds))
        return filters
    }

    suspend fun handleFilterDuplicates(
        currentFilters: List<RecordsFilter>,
        recordsViewData: RecordsFilterSelectedRecordsViewData?,
    ): List<RecordsFilter> {
        if (recordsViewData == null || recordsViewData.isLoading) return currentFilters

        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.ManuallyFiltered }
        val records = recordFilterInteractor.getByFilter(filters)
        val result = getDuplicatedRecordsInteractor.execute(
            filters = filters.getDuplicationItems(),
            records = records,
        )
        val selectedIds = recordsViewData
            .recordsViewData
            .mapNotNull {
                if (it !is RecordViewData.Tracked) return@mapNotNull null
                if (it.id in result.duplications) it.id else null
            }

        if (selectedIds.isNotEmpty()) filters.add(RecordsFilter.ManuallyFiltered(selectedIds))
        return filters
    }

    fun onDurationSet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Duration }
        filters.add(RecordsFilter.Duration(Range(rangeStart, rangeEnd)))
        return filters
    }

    fun handleDateSet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val range = Range(rangeStart, rangeEnd)
        filters.removeAll { it is RecordsFilter.Date }
        filters.add(RecordsFilter.Date(RangeLength.Custom(range), 0))
        return filters
    }

    fun handleRangeSet(
        currentFilters: List<RecordsFilter>,
        itemType: FilterViewData.Type,
        currentRange: Range,
    ): List<RecordsFilter> {
        val rangeLength = (itemType as? RecordFilterDateType)?.rangeLength
            ?: return currentFilters
        val newRange = if (rangeLength is RangeLength.Custom) {
            val newCustomRange = Range(currentRange.timeStarted, currentRange.timeEnded)
            RangeLength.Custom(newCustomRange)
        } else {
            rangeLength
        }
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Date }
        filters.add(RecordsFilter.Date(newRange, 0))
        return filters
    }

    fun handleTimeOfDaySet(
        currentFilters: List<RecordsFilter>,
        rangeStart: Long,
        rangeEnd: Long,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.TimeOfDay }
        filters.add(RecordsFilter.TimeOfDay(Range(rangeStart, rangeEnd)))
        return filters
    }

    fun removeFilter(
        currentFilters: List<RecordsFilter>,
        type: RecordFilterType,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val filterClass = recordsFilterViewDataMapper.mapToClass(type)
        filters.removeAll { filterClass.isInstance(it) }
        return filters
    }

    fun handleDayOfWeekClick(
        currentFilters: List<RecordsFilter>,
        dayOfWeek: DayOfWeek,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        val newDays = filters.getDaysOfWeek()
            .toMutableList()
            .apply { addOrRemove(dayOfWeek) }

        filters.removeAll { it is RecordsFilter.DaysOfWeek }
        if (newDays.isNotEmpty()) filters.add(RecordsFilter.DaysOfWeek(newDays))
        return filters
    }

    fun onTypesSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        recordTypes: List<RecordType>,
    ): List<RecordsFilter> {
        val newIds = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> recordTypes.map { it.id }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> emptyList()
        }
        return handleSelectTypes(
            currentFilters = currentFilters,
            newIds = newIds,
        )
    }

    fun onCategoriesSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        categories: List<Category>,
    ): List<RecordsFilter> {
        val newItems = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> {
                categories
                    .map { RecordsFilter.CategoryItem.Categorized(it.id) }
                    .plus(RecordsFilter.CategoryItem.Uncategorized)
            }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> {
                emptyList()
            }
        }
        return handleSelectCategories(
            currentFilters = currentFilters,
            newItems = newItems,
        )
    }

    fun onTagsSelectionButtonClick(
        currentFilters: List<RecordsFilter>,
        subtype: RecordsFilterSelectionButtonType.Subtype,
        currentState: RecordFilterType,
        tags: List<RecordTag>,
    ): List<RecordsFilter> {
        val newItems = when (subtype) {
            is RecordsFilterSelectionButtonType.Subtype.SelectAll -> {
                tags
                    .map { RecordsFilter.TagItem.Tagged(it.id) }
                    .plus(RecordsFilter.TagItem.Untagged)
            }
            is RecordsFilterSelectionButtonType.Subtype.SelectNone -> {
                emptyList()
            }
        }
        return handleSelectTags(
            currentState = currentState,
            currentFilters = currentFilters,
            newItems = newItems,
        )
    }

    fun checkTagFilterConsistency(
        currentFilters: List<RecordsFilter>,
        recordTypes: List<RecordType>,
        recordTypeCategories: List<RecordTypeCategory>,
        recordTags: List<RecordTag>,
        typesToTags: List<RecordTypeToTag>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        // Update tags according to selected activities
        val newTypeIds: List<Long> = filters.getAllTypeIds(
            recordTypes = recordTypes,
            recordTypeCategories = recordTypeCategories,
        )

        fun update(tags: List<RecordsFilter.TagItem>): List<RecordsFilter.TagItem> {
            return tags.filter {
                when (it) {
                    is RecordsFilter.TagItem.Tagged -> {
                        it.tagId in recordTags
                            .map { tag -> tag.id }
                            .let { tags ->
                                filterSelectableTagsInteractor.execute(
                                    tagIds = tags,
                                    typesToTags = typesToTags,
                                    typeIds = newTypeIds,
                                )
                            }
                    }
                    is RecordsFilter.TagItem.Untagged -> {
                        true
                    }
                }
            }
        }

        val newSelectedTags = update(filters.getSelectedTags())

        filters.removeAll { filter -> filter is RecordsFilter.SelectedTags }
        if (newSelectedTags.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newSelectedTags))

        val newFilteredTags = update(filters.getFilteredTags())

        filters.removeAll { filter -> filter is RecordsFilter.FilteredTags }
        if (newFilteredTags.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newFilteredTags))

        return filters
    }

    private fun handleSelectTypes(
        currentFilters: List<RecordsFilter>,
        newIds: List<Long>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newIds.isNotEmpty()) filters.add(RecordsFilter.Activity(newIds))
        return filters
    }

    private fun handleSelectCategories(
        currentFilters: List<RecordsFilter>,
        newItems: List<RecordsFilter.CategoryItem>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Activity }
        filters.removeAll { it is RecordsFilter.Category }
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        if (newItems.isNotEmpty()) filters.add(RecordsFilter.Category(newItems))
        return filters
    }

    private fun handleSelectTags(
        currentState: RecordFilterType,
        currentFilters: List<RecordsFilter>,
        newItems: List<RecordsFilter.TagItem>,
    ): List<RecordsFilter> {
        val filters = currentFilters.toMutableList()
        filters.removeAll { it is RecordsFilter.Untracked }
        filters.removeAll { it is RecordsFilter.Multitask }
        when (currentState) {
            RecordFilterType.SelectedTags -> {
                filters.removeAll { it is RecordsFilter.SelectedTags }
                if (newItems.isNotEmpty()) filters.add(RecordsFilter.SelectedTags(newItems))
            }
            RecordFilterType.FilteredTags -> {
                filters.removeAll { it is RecordsFilter.FilteredTags }
                if (newItems.isNotEmpty()) filters.add(RecordsFilter.FilteredTags(newItems))
            }
            else -> return currentFilters
        }
        return filters
    }
}