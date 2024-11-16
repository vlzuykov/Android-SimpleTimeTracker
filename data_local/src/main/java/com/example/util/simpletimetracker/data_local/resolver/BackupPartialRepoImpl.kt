package com.example.util.simpletimetracker.data_local.resolver

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl.DataHandler
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.BackupOptionsData
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ComplexRule
import com.example.util.simpletimetracker.domain.model.FavouriteColor
import com.example.util.simpletimetracker.domain.model.FavouriteComment
import com.example.util.simpletimetracker.domain.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordToRecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.RecordTypeToDefaultTag
import com.example.util.simpletimetracker.domain.model.RecordTypeToTag
import com.example.util.simpletimetracker.domain.model.getExistingValues
import com.example.util.simpletimetracker.domain.model.getNotExistingValues
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteColorRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.resolver.BackupPartialRepo
import com.example.util.simpletimetracker.domain.resolver.ResultCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupPartialRepoImpl @Inject constructor(
    private val backupRepo: BackupRepoImpl,
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTagRepo: RecordTagRepo,
    private val activityFilterRepo: ActivityFilterRepo,
    private val favouriteCommentRepo: FavouriteCommentRepo,
    private val favouriteColorRepo: FavouriteColorRepo,
    private val favouriteIconRepo: FavouriteIconRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val complexRuleRepo: ComplexRuleRepo,
    private val resourceRepo: ResourceRepo,
) : BackupPartialRepo {

    // Replace original id with 0 to add instead of replacing.
    // Replace original ids in other data with actual id after adding.
    override suspend fun partialRestoreBackupFile(
        params: BackupOptionsData.Custom,
    ): ResultCode = withContext(Dispatchers.IO) {
        val originalTypeIdToAddedId: MutableMap<Long, Long> = params.data.types
            .values.getExistingValues().associate { it.id to it.id }.toMutableMap()
        val originalCategoryIdToAddedId: MutableMap<Long, Long> = params.data.categories
            .values.getExistingValues().associate { it.id to it.id }.toMutableMap()
        val originalTagIdToAddedId: MutableMap<Long, Long> = params.data.tags
            .values.getExistingValues().associate { it.id to it.id }.toMutableMap()
        val originalRecordIdToAddedId: MutableMap<Long, Long> = params.data.records
            .values.getExistingValues().associate { it.id to it.id }.toMutableMap()

        params.data.types.values.getNotExistingValues().forEach { type ->
            val originalId = type.id
            val addedId = type.copy(
                id = 0,
            ).let { recordTypeRepo.add(it) }
            originalTypeIdToAddedId[originalId] = addedId
        }
        params.data.records.values.getNotExistingValues().forEach { record ->
            val originalId = record.id
            val newTypeId = originalTypeIdToAddedId[record.typeId]
                ?: return@forEach
            val addedId = record.copy(
                id = 0,
                typeId = newTypeId,
            ).let { recordRepo.add(it) }
            originalRecordIdToAddedId[originalId] = addedId
        }
        params.data.categories.values.getNotExistingValues().forEach { category ->
            val originalId = category.id
            val addedId = category.copy(
                id = 0,
            ).let { categoryRepo.add(it) }
            originalCategoryIdToAddedId[originalId] = addedId
        }
        params.data.typeToCategory.getNotExistingValues().forEach { typeToCategory ->
            val newTypeId = originalTypeIdToAddedId[typeToCategory.recordTypeId]
                ?: return@forEach
            val newCategoryId = originalCategoryIdToAddedId[typeToCategory.categoryId]
                ?: return@forEach
            typeToCategory.copy(
                recordTypeId = newTypeId,
                categoryId = newCategoryId,
            ).let { recordTypeCategoryRepo.add(it) }
        }
        params.data.tags.values.getNotExistingValues().forEach { tag ->
            val originalId = tag.id
            val newColorSource = originalTypeIdToAddedId[tag.iconColorSource].orZero()
            val addedId = tag.copy(
                id = 0,
                iconColorSource = newColorSource,
            ).let { recordTagRepo.add(it) }
            originalTagIdToAddedId[originalId] = addedId
        }
        params.data.recordToTag.getNotExistingValues().forEach { recordToTag ->
            val newRecordId = originalRecordIdToAddedId[recordToTag.recordId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[recordToTag.recordTagId]
                ?: return@forEach
            recordToTag.copy(
                recordId = newRecordId,
                recordTagId = newTagId,
            ).let { recordToRecordTagRepo.add(it) }
        }
        params.data.typeToTag.getNotExistingValues().forEach { typeToTag ->
            val newTypeId = originalTypeIdToAddedId[typeToTag.recordTypeId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[typeToTag.tagId]
                ?: return@forEach
            typeToTag.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            ).let { recordTypeToTagRepo.add(it) }
        }
        params.data.typeToDefaultTag.getNotExistingValues().forEach { typeToDefaultTag ->
            val newTypeId = originalTypeIdToAddedId[typeToDefaultTag.recordTypeId]
                ?: return@forEach
            val newTagId = originalTagIdToAddedId[typeToDefaultTag.tagId]
                ?: return@forEach
            typeToDefaultTag.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            ).let { recordTypeToDefaultTagRepo.add(it) }
        }
        params.data.activityFilters.values.getNotExistingValues().forEach { activityFilter ->
            val newTypeIds = activityFilter.selectedIds
                .mapNotNull { originalTypeIdToAddedId[it] }
            activityFilter.copy(
                id = 0,
                selectedIds = newTypeIds,
            ).let { activityFilterRepo.add(it) }
        }
        params.data.favouriteComments.values.getNotExistingValues().forEach { favComment ->
            favComment.copy(
                id = 0,
            ).let { favouriteCommentRepo.add(it) }
        }
        params.data.favouriteColors.values.getNotExistingValues().forEach { favColor ->
            favColor.copy(
                id = 0,
            ).let { favouriteColorRepo.add(it) }
        }
        params.data.favouriteIcon.values.getNotExistingValues().forEach { favIcon ->
            favIcon.copy(
                id = 0,
            ).let { favouriteIconRepo.add(it) }
        }
        params.data.goals.values.getNotExistingValues().forEach { goal ->
            val newId = when (val idData = goal.idData) {
                is RecordTypeGoal.IdData.Type -> originalTypeIdToAddedId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Type)
                is RecordTypeGoal.IdData.Category -> originalCategoryIdToAddedId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Category)
            } ?: return@forEach
            goal.copy(
                id = 0,
                idData = newId,
            ).let { recordTypeGoalRepo.add(it) }
        }
        params.data.rules.values.getNotExistingValues().forEach { rule ->
            val newStartingTypeIds = rule.conditionStartingTypeIds
                .mapNotNull { originalTypeIdToAddedId[it] }.toSet()
            val newCurrentTypeIds = rule.conditionCurrentTypeIds
                .mapNotNull { originalTypeIdToAddedId[it] }.toSet()
            val newAssignTagIds = rule.actionAssignTagIds
                .mapNotNull { originalTagIdToAddedId[it] }.toSet()
            rule.copy(
                id = 0,
                actionAssignTagIds = newAssignTagIds,
                conditionStartingTypeIds = newStartingTypeIds,
                conditionCurrentTypeIds = newCurrentTypeIds,
            ).takeIf {
                it.hasActions && it.hasConditions
            }?.let { complexRuleRepo.add(it) }
        }
        return@withContext ResultCode.Success(
            resourceRepo.getString(R.string.message_import_complete),
        )
    }

    /**
     * Marks data from the backup file if it is already exist,
     * to skip later on import.
     */
    @Suppress("ComplexRedundantLet")
    override suspend fun readBackupFile(
        uriString: String,
    ): Pair<ResultCode, PartialBackupRestoreData?> = withContext(Dispatchers.IO) {
        // Result data
        val types: MutableList<RecordType> = mutableListOf()
        val typesCurrent: List<RecordType> = recordTypeRepo.getAll()
        val records: MutableList<Record> = mutableListOf()
        val recordsCurrent: List<Record> = recordRepo.getAll().map { it.copy(tagIds = emptyList()) }
        val categories: MutableList<Category> = mutableListOf()
        val categoriesCurrent: List<Category> = categoryRepo.getAll()
        val typeToCategory: MutableList<RecordTypeCategory> = mutableListOf()
        val typeToCategoryCurrent: List<RecordTypeCategory> = recordTypeCategoryRepo.getAll()
        val tags: MutableList<RecordTag> = mutableListOf()
        val tagsCurrent: List<RecordTag> = recordTagRepo.getAll()
        val recordToTag: MutableList<RecordToRecordTag> = mutableListOf()
        val recordToTagCurrent: List<RecordToRecordTag> = recordToRecordTagRepo.getAll()
        val typeToTag: MutableList<RecordTypeToTag> = mutableListOf()
        val typeToTagCurrent: List<RecordTypeToTag> = recordTypeToTagRepo.getAll()
        val typeToDefaultTag: MutableList<RecordTypeToDefaultTag> = mutableListOf()
        val typeToDefaultTagCurrent: List<RecordTypeToDefaultTag> = recordTypeToDefaultTagRepo.getAll()
        val activityFilters: MutableList<ActivityFilter> = mutableListOf()
        val activityFiltersCurrent: List<ActivityFilter> = activityFilterRepo.getAll()
        val favouriteComments: MutableList<FavouriteComment> = mutableListOf()
        val favouriteCommentsCurrent: List<FavouriteComment> = favouriteCommentRepo.getAll()
        val favouriteColors: MutableList<FavouriteColor> = mutableListOf()
        val favouriteColorsCurrent: List<FavouriteColor> = favouriteColorRepo.getAll()
        val favouriteIcon: MutableList<FavouriteIcon> = mutableListOf()
        val favouriteIconCurrent: List<FavouriteIcon> = favouriteIconRepo.getAll()
        val goals: MutableList<RecordTypeGoal> = mutableListOf()
        val goalsCurrent: List<RecordTypeGoal> = recordTypeGoalRepo.getAllTypeGoals()
        val rules: MutableList<ComplexRule> = mutableListOf()
        val rulesCurrent: List<ComplexRule> = complexRuleRepo.getAll()
        val settings: MutableList<List<String>> = mutableListOf()

        val result = backupRepo.readBackup(
            uriString = uriString,
            successCodeMessage = null,
            errorCodeMessage = R.string.settings_file_open_error,
            clearData = false,
            migrateTags = {
                tags += backupRepo.migrateTags(
                    types = types,
                    data = it,
                )
            },
            dataHandler = DataHandler(
                types = types::add,
                records = records::add,
                categories = categories::add,
                typeToCategory = typeToCategory::add,
                tags = tags::add,
                recordToTag = recordToTag::add,
                typeToTag = typeToTag::add,
                typeToDefaultTag = typeToDefaultTag::add,
                activityFilters = activityFilters::add,
                favouriteComments = favouriteComments::add,
                favouriteColors = favouriteColors::add,
                favouriteIcon = favouriteIcon::add,
                goals = goals::add,
                rules = rules::add,
                settings = settings::add,
            ),
        )

        // If type already exist - need to replace typeId to existing typeId in records etc.
        val (newTypes, originalTypeIdToExistingId) = types.let {
            mapToHolder(it, typesCurrent)
        }

        val (newRecords, originalRecordIdToExistingId) = records.mapNotNull { item ->
            val newTypeId = originalTypeIdToExistingId[item.typeId]
                ?: return@mapNotNull null
            item.copy(typeId = newTypeId)
        }.let {
            mapToHolder(it, recordsCurrent)
        }

        val (newCategories, originalCategoryIdToExistingId) = categories.let {
            mapToHolder(it, categoriesCurrent)
        }

        val newTypeToCategory = typeToCategory.mapNotNull { item ->
            val newTypeId = originalTypeIdToExistingId[item.recordTypeId]
                ?: return@mapNotNull null
            val newCategoryId = originalCategoryIdToExistingId[item.categoryId]
                ?: return@mapNotNull null
            item.copy(
                recordTypeId = newTypeId,
                categoryId = newCategoryId,
            )
        }.let {
            mapToHolder(it, typeToCategoryCurrent)
        }.list

        val (newTags, originalTagIdToExistingId) = tags.map { item ->
            val newColorSource = originalTypeIdToExistingId[item.iconColorSource].orZero()
            item.copy(
                iconColorSource = newColorSource,
            )
        }.let {
            mapToHolder(it, tagsCurrent)
        }

        val newRecordToTag = recordToTag.mapNotNull { item ->
            val newRecordId = originalRecordIdToExistingId[item.recordId]
                ?: return@mapNotNull null
            val newTagId = originalTagIdToExistingId[item.recordTagId]
                ?: return@mapNotNull null
            item.copy(
                recordId = newRecordId,
                recordTagId = newTagId,
            )
        }.let {
            mapToHolder(it, recordToTagCurrent)
        }.list

        val newTypeToTag = typeToTag.mapNotNull { item ->
            val newTypeId = originalTypeIdToExistingId[item.recordTypeId]
                ?: return@mapNotNull null
            val newTagId = originalTagIdToExistingId[item.tagId]
                ?: return@mapNotNull null
            item.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            )
        }.let {
            mapToHolder(it, typeToTagCurrent)
        }.list

        val newTypeToDefaultTag = typeToDefaultTag.mapNotNull { item ->
            val newTypeId = originalTypeIdToExistingId[item.recordTypeId]
                ?: return@mapNotNull null
            val newTagId = originalTagIdToExistingId[item.tagId]
                ?: return@mapNotNull null
            item.copy(
                recordTypeId = newTypeId,
                tagId = newTagId,
            )
        }.let {
            mapToHolder(it, typeToDefaultTagCurrent)
        }.list

        val newActivityFilters = activityFilters.map { item ->
            val newTypeIds = item.selectedIds.mapNotNull {
                if (item.type is ActivityFilter.Type.Activity) {
                    originalTypeIdToExistingId[it]
                } else {
                    it
                }
            }
            item.copy(
                selectedIds = newTypeIds,
            )
        }.let {
            mapToHolder(it, activityFiltersCurrent)
        }.list

        val newFavouriteComments = favouriteComments.let {
            mapToHolder(it, favouriteCommentsCurrent)
        }.list

        val newFavouriteColors = favouriteColors.let {
            mapToHolder(it, favouriteColorsCurrent)
        }.list

        val newFavouriteIcon = favouriteIcon.let {
            mapToHolder(it, favouriteIconCurrent)
        }.list

        val newGoals = goals.mapNotNull { item ->
            val newId = when (val idData = item.idData) {
                is RecordTypeGoal.IdData.Type -> originalTypeIdToExistingId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Type)
                is RecordTypeGoal.IdData.Category -> originalCategoryIdToExistingId[idData.value]
                    ?.let(RecordTypeGoal.IdData::Category)
            } ?: return@mapNotNull null
            item.copy(
                idData = newId,
            )
        }.let {
            mapToHolder(it, goalsCurrent)
        }.list

        val newRules = rules.mapNotNull { item ->
            val newStartingTypeIds = item.conditionStartingTypeIds
                .mapNotNull { originalTypeIdToExistingId[it] }.toSet()
            val newCurrentTypeIds = item.conditionCurrentTypeIds
                .mapNotNull { originalTypeIdToExistingId[it] }.toSet()
            val newAssignTagIds = item.actionAssignTagIds
                .mapNotNull { originalTagIdToExistingId[it] }.toSet()
            item.copy(
                actionAssignTagIds = newAssignTagIds,
                conditionStartingTypeIds = newStartingTypeIds,
                conditionCurrentTypeIds = newCurrentTypeIds,
            ).takeIf {
                it.hasActions && it.hasConditions
            }
        }.let {
            mapToHolder(it, rulesCurrent)
        }.list

        // Fill tags after all data processed, with actual tagIds.
        val newRecordToTagMap = newRecordToTag.groupBy {
            it.data.recordId
        }
        val newRecordsWithTags = newRecords.map { record ->
            val thisTags = newRecordToTagMap[record.data.id].orEmpty().map { it.data }
            val newData = record.data.copy(tagIds = thisTags.map(RecordToRecordTag::recordTagId))
            record.copy(data = newData)
        }

        result to PartialBackupRestoreData(
            types = newTypes.associateBy { it.data.id },
            records = newRecordsWithTags.associateBy { it.data.id },
            categories = newCategories.associateBy { it.data.id },
            typeToCategory = newTypeToCategory,
            tags = newTags.associateBy { it.data.id },
            recordToTag = newRecordToTag,
            typeToTag = newTypeToTag,
            typeToDefaultTag = newTypeToDefaultTag,
            activityFilters = newActivityFilters.associateBy { it.data.id },
            favouriteComments = newFavouriteComments.associateBy { it.data.id },
            favouriteColors = newFavouriteColors.associateBy { it.data.id },
            favouriteIcon = newFavouriteIcon.associateBy { it.data.id },
            goals = newGoals.associateBy { it.data.id },
            rules = newRules.associateBy { it.data.id },
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getIdData(
        value: T?,
    ): IdData<T> {
        return when (value) {
            is RecordType -> IdData<RecordType>({ copy(id = it) }, { id })
            is Record -> IdData<Record>({ copy(id = it) }, { id })
            is Category -> IdData<Category>({ copy(id = it) }, { id })
            is RecordTypeCategory -> IdData<RecordTypeCategory>({ this }, { 0 })
            is RecordTag -> IdData<RecordTag>({ copy(id = it) }, { id })
            is RecordToRecordTag -> IdData<RecordToRecordTag>({ this }, { 0 })
            is RecordTypeToTag -> IdData<RecordTypeToTag>({ this }, { 0 })
            is RecordTypeToDefaultTag -> IdData<RecordTypeToDefaultTag>({ this }, { 0 })
            is ActivityFilter -> IdData<ActivityFilter>({ copy(id = it) }, { id })
            is FavouriteComment -> IdData<FavouriteComment>({ copy(id = it) }, { id })
            is FavouriteColor -> IdData<FavouriteColor>({ copy(id = it) }, { id })
            is FavouriteIcon -> IdData<FavouriteIcon>({ copy(id = it) }, { id })
            is RecordTypeGoal -> IdData<RecordTypeGoal>({ copy(id = it) }, { id })
            is ComplexRule -> IdData<ComplexRule>({ copy(id = it) }, { id })
            else -> IdData({ this }, { 0 })
        } as IdData<T>
    }

    private fun <T> mapToHolder(
        dataFromFile: List<T>,
        currentData: List<T>,
    ): ReadData<T> {
        if (dataFromFile.isEmpty()) return ReadData(emptyList(), emptyMap())
        val idData = getIdData(dataFromFile.firstOrNull())
        val replaceId: T.(Long) -> T = { id -> idData.idSetter(this, id) }
        val id: T.() -> Long = { idData.idGetter(this) }
        val currentDataClean: Map<T, Long> = currentData.associate { it.replaceId(0) to it.id() }
        val originalIdsToExistingId = mutableMapOf<Long, Long>()
        val list = dataFromFile.map { item ->
            val cleanItem = item.replaceId(0)
            val existingId = currentDataClean[cleanItem]
            val itemId = item.id()
            if (itemId != 0L) {
                originalIdsToExistingId[itemId] = existingId ?: itemId
            }
            val newItem = if (existingId != null) {
                item.replaceId(existingId)
            } else {
                item
            }
            PartialBackupRestoreData.Holder(
                exist = existingId != null,
                data = newItem,
            )
        }
        return ReadData(list, originalIdsToExistingId)
    }

    private data class ReadData<T>(
        val list: List<PartialBackupRestoreData.Holder<T>>,
        val originalIdsToExistingId: Map<Long, Long>,
    )

    private data class IdData<T>(
        val idSetter: T.(Long) -> T,
        val idGetter: T.() -> Long,
    )
}