package com.example.util.simpletimetracker.domain.category.interactor

import com.example.util.simpletimetracker.domain.category.model.RecordTypeCategory
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import javax.inject.Inject

class RecordTypeCategoryInteractor @Inject constructor(
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
) {

    suspend fun getAll(): List<RecordTypeCategory> {
        return recordTypeCategoryRepo.getAll()
    }

    suspend fun getCategories(typeId: Long): Set<Long> {
        return recordTypeCategoryRepo.getCategoryIdsByType(typeId)
    }

    suspend fun addCategories(typeId: Long, categoryIds: List<Long>) {
        recordTypeCategoryRepo.addCategories(typeId, categoryIds)
    }

    suspend fun removeCategories(typeId: Long, categoryIds: List<Long>) {
        recordTypeCategoryRepo.removeCategories(typeId, categoryIds)
    }

    suspend fun getTypes(categoryId: Long): Set<Long> {
        return recordTypeCategoryRepo.getTypeIdsByCategory(categoryId)
    }

    suspend fun addTypes(categoryId: Long, typeIds: List<Long>) {
        recordTypeCategoryRepo.addTypes(categoryId, typeIds)
    }

    suspend fun removeTypes(categoryId: Long, typeIds: List<Long>) {
        recordTypeCategoryRepo.removeTypes(categoryId, typeIds)
    }
}