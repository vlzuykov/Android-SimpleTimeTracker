package com.example.util.simpletimetracker.data_local.activitySuggestion

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ActivitySuggestionDao {

    @Transaction
    @Query("SELECT * FROM activitySuggestion")
    suspend fun getAll(): List<ActivitySuggestionDBO>

    @Transaction
    @Query("SELECT * FROM activitySuggestion WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): ActivitySuggestionDBO?

    @Transaction
    @Query("SELECT * FROM activitySuggestion WHERE :typeId in (forTypeId)")
    suspend fun getByTypeId(typeId: Long): List<ActivitySuggestionDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityFilters: List<ActivitySuggestionDBO>)

    @Query("DELETE FROM activitySuggestion WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM activitySuggestion")
    suspend fun clear()
}