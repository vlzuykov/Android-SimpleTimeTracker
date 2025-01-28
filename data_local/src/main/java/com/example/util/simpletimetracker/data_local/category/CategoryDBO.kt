package com.example.util.simpletimetracker.data_local.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color")
    val color: Int,

    // If not empty - take color from here, custom colorInt stored as text.
    @ColumnInfo(name = "color_int")
    val colorInt: String,

    @ColumnInfo(name = "note")
    val note: String,
)