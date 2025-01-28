package com.example.util.simpletimetracker.data_local.category

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordTypeCategoryDao {

    @Query("SELECT * FROM recordTypeCategory")
    suspend fun getAll(): List<RecordTypeCategoryDBO>

    @Query("SELECT category_id FROM recordTypeCategory WHERE record_type_id = :typeId")
    suspend fun getCategoryIdsByType(typeId: Long): List<Long>

    @Query("SELECT record_type_id FROM recordTypeCategory WHERE category_id = :categoryId")
    suspend fun getTypeIdsByCategory(categoryId: Long): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recordTypeCategoryDBO: List<RecordTypeCategoryDBO>)

    @Delete
    suspend fun delete(recordTypeCategoryDBO: List<RecordTypeCategoryDBO>)

    @Query("DELETE FROM recordTypeCategory WHERE category_id = :categoryId")
    suspend fun deleteAll(categoryId: Long)

    @Query("DELETE FROM recordTypeCategory WHERE record_type_id = :typeId")
    suspend fun deleteAllByType(typeId: Long)

    @Query("DELETE FROM recordTypeCategory")
    suspend fun clear()
}